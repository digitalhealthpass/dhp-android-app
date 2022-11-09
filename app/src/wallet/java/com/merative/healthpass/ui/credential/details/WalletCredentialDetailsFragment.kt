package com.merative.healthpass.ui.credential.details

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.merative.healthpass.BuildConfig
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragmentCredentialDetailsBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.region.getEnvironmentTitle
import com.merative.healthpass.models.results.VerificationResultsModel
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.models.sharedPref.getIssuerName
import com.merative.healthpass.models.sharedPref.getSchemaName
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import com.merative.healthpass.ui.credential.barcode.FullScreenQRFragment
import com.merative.healthpass.ui.credential.selectConnection.SelectConnectionFragment
import com.merative.healthpass.ui.credential.sharing.ObfuscationSettingsFragment
import com.merative.healthpass.ui.credential.source.CredentialSourceFragment
import com.merative.healthpass.ui.home.ContactsSection
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.healthpass.utils.schema.*
import com.merative.watson.healthpass.verifiablecredential.extensions.*
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.cose.cwt
import com.merative.watson.healthpass.verifiablecredential.models.cwt.CWT
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.expiryDate
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.isIDHPorGHPorVC
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.issuanceDate
import com.merative.watson.healthpass.verificationengine.VerifyEngine
import com.merative.watson.healthpass.verificationengine.exception.VerifyError
import com.merative.watson.healthpass.verificationengine.expiration.isExpired
import com.samsung.android.sdk.samsungpay.v2.PartnerInfo
import com.samsung.android.sdk.samsungpay.v2.SamsungPay
import com.samsung.android.sdk.samsungpay.v2.SpaySdk
import com.samsung.android.sdk.samsungpay.v2.StatusListener
import com.samsung.android.sdk.samsungpay.v2.card.AddCardInfo
import com.samsung.android.sdk.samsungpay.v2.card.AddCardListener
import com.samsung.android.sdk.samsungpay.v2.card.Card
import com.samsung.android.sdk.samsungpay.v2.card.CardManager
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.kotlin.addTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit


class WalletCredentialDetailsFragment : CredentialDetailsFragment() {
    private var _binding: FragmentCredentialDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: CredentialDetailsVM
    private val deleteDisposable = SerialDisposable()
    private val updateDisposable = SerialDisposable()
    private val sectionAdapter = SectionedRecyclerViewAdapter()
    private val connectionSectionAdapter = SectionedRecyclerViewAdapter()
    private val cardsInfoSectionAdapter = SectionedRecyclerViewAdapter()
    private val gsonBuilder = GsonBuilder().disableHtmlEscaping().create()

    private lateinit var contactsSection: ContactsSection
    private val loadingDisposable = CompositeDisposable()

    private var profileCredId: String? = null
    private var verifiableObject: VerifiableObject? = null

    private lateinit var valueMapper: Map<String, Map<String, String>>
    private var selectedCredentials = ArrayList<Package>()
    private var resultsModel: VerificationResultsModel = VerificationResultsModel()

    private val disposables = CompositeDisposable()
    private lateinit var verifiableEngine: VerifyEngine

    private val dhpSupportedTypes = listOf("vaccination", "test")
    private val shcSupportedTypes = listOf("immunization", "covid19", "observation")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentCredentialDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(CredentialDetailsVM::class.java)
        valueMapper = parse(loadValueMapperJson())

        getCredentialAndSchema()?.let { selectedCredentials.add(it) }
        adjustClicks(view)
        //TODO SET this to true when obfuscation and sharing story comes back
        setHasOptionsMenu(false)
        initUi(view)
        if (BuildConfig.SPAY_ENABLED) {
            //binding.addSamsungPayBtn.visibility = View.VISIBLE
            setUpSamsungPay(true)
        }

        loadData()
        shareCard()
    }

    private fun shareCard() {
        binding.shareCardBtn.setOnClickListener {
            findNavController().navigate(
                R.id.action_credential_details_to_select_connection_fragment,
                bundleOf(SelectConnectionFragment.KEY_CREDENTIAL_LIST to selectedCredentials)
            )
        }
    }

    private fun adjustClicks(root: View) {
        binding.credDetailsBtnDelete.setOnClickListener {
            showDeleteAlertDialog()
        }

        binding.viewSourceBtn.setOnClickListener {
            showCredentialSource()
        }

        binding.addSamsungPayBtn.setOnClickListener {
            setUpSamsungPay(false)
        }
    }

    private fun getCredentialAndSchema(): Package? {
        return arguments?.get(KEY_CREDENTIAL_PACKAGE) as? Package
    }


    private fun addCredentialToSamsungPay() {

        getCredentialAndSchema()?.verifiableObject?.let {

            val bundle = Bundle()
            bundle.putString(
                SamsungPay.PARTNER_SERVICE_TYPE,
                SpaySdk.ServiceType.APP2APP.toString()
            )
            val pInfo = PartnerInfo(BuildConfig.SPAY_SERVICEID, bundle)
            val cardManager = CardManager(context, pInfo)

            val payload = createSamsungPayBundle(it)
            if (payload == "ERROR") return

            val cardDetail = Bundle()
            cardDetail.putString(AddCardInfo.EXTRA_PROVISION_PAYLOAD, payload)

            val cardType = Card.CARD_TYPE_VACCINE_PASS
            val tokenizationProvider = AddCardInfo.PROVIDER_VACCINE_PASS
            val addCardInfo = AddCardInfo(cardType, tokenizationProvider, cardDetail)

            cardManager.addCard(addCardInfo, object : AddCardListener {
                override fun onSuccess(status: Int, card: Card) {
                    Log.d(TAG, "onSuccess callback is called")
                }

                override fun onFail(error: Int, errorData: Bundle) {
                    Log.d(TAG, "doAddCard onFail callback is called, errorCode: $error")
                    val extraReason = errorData.getInt(SamsungPay.EXTRA_ERROR_REASON)
                    Log.e(TAG, "doAddCard onFail extra reason extra code: $extraReason")
                    if (errorData.containsKey(SpaySdk.EXTRA_ERROR_REASON_MESSAGE)) {
                        Log.e(
                            TAG, "doAddCard onFail extra reason message: "
                                    + errorData.getString(SpaySdk.EXTRA_ERROR_REASON_MESSAGE)
                        )
                    }
                    Toast.makeText(
                        context,
                        "Something went wrong. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onProgress(currentCount: Int, totalCount: Int, bundleData: Bundle) {
                    Log.d(TAG, "onProgress callback is called : $currentCount / $totalCount")
                }
            })

        }
    }

    private fun createSamsungPayBundle(it: VerifiableObject): String {
        return when (it.type) {
            VCType.SHC -> shcSchema(it)
            VCType.DCC -> dccSchema(it)
            VCType.GHP, VCType.IDHP, VCType.VC -> dhpSchema(it)
            else -> {
                Toast.makeText(context, "Card type not supported", Toast.LENGTH_SHORT).show()
                binding.addSamsungPayBtn.visibility = View.GONE
                "ERROR"
            }
        }
    }

    private fun dccSchema(it: VerifiableObject): String {
        val issuer = getCredentialAndSchema()?.let {
            it.getIssuerName().ifEmpty { "Unknown Issuer" }
        }
        val payloadObject = it.cose?.hCertJson?.toJsonElement().asJsonObjectOrNull()
        val names = payloadObject?.getAsJsonObject("nam")
        val familyName = names?.getStringOrNull("fn") ?: names?.getStringOrNull("fnt")
        val givenName = names?.getStringOrNull("gn") ?: names?.getStringOrNull("gnt")
        val dob = payloadObject?.getStringOrNull("dob")?.replace("-", "")
        val payloadMap = mutableMapOf(
            "version" to SPAY_SCHEMA_VERSION,
            "cardId" to it.cose?.keyId,
            "issuer" to mapOf("name" to issuer),
            "qrData" to mutableMapOf(
                "decoding" to "None",
                "chunks" to listOf(it.rawString)
            ),
            "expirationDate" to it.cose?.cwt?.exp?.let { TimeUnit.SECONDS.toMillis(it) }
                ?.timestampToDate()
                ?.format(SPAY_DATE_FORMAT),
            "provider" to mapOf("name" to SPAY_PROVIDER_NAME),
            "patient" to mapOf(
                "dateOfBirth" to dob,
                "name" to mapOf(
                    "family" to familyName,
                    "given" to givenName
                )
            )
        )
        with (payloadObject) {
            if (this?.has("v") == true) {
                "v"
            } else {
                if (this?.has("t") == true) "t"
                else null
            }
        }?.let { type ->
            val credentials = payloadObject?.getAsJsonArray(type)
            if (type == "v") {
                payloadMap["cardArt"] = SPAY_COMPANY_VACCINE_CARD_ART
                payloadMap["type"] = "VaccinationRecordCard"
                payloadMap["types"] = setOf("VaccinationRecordCard")
                val meta = mutableListOf<Map<String, String?>>()
                credentials?.map { c ->
                    val credential = c.asJsonObject
                    val doseNumber = credential.getStringOrNull("dn")
                    val doseTotal = credential.getStringOrNull("sd")
                    val product = valueMapper["vaccine-medicinal-product-dcc"]
                        ?.get(credential.getStringOrNull("mp"))
                    val date = credential.getStringOrNull("dt")
                        ?.replace("-", "")
                    meta.add(mapOf(
                        "doseNumber" to doseNumber,
                        "doseTotal" to doseTotal,
                        "product" to product,
                        "date" to date
                    ))
                    mapOf(
                        "vaccine" to mapOf(
                            "product" to product,
                            "disease" to valueMapper["disease-agent-targeted-dcc"]?.getOrDefault(
                                credential?.getStringOrNull("tg"),
                                "COVID-19"
                            ),
                            "date" to date
                        ),
                        "performer" to mapOf(
                            "name" to credential.getStringOrNull("is"),
                            "identityAssuranceLevel" to "IAL1.2"
                        ),
                        "dose" to mapOf(
                            "number" to doseNumber,
                            "total" to doseTotal
                        )
                    )
                }?.let { v -> payloadMap["vaccinations"] = v }
                (payloadMap["qrData"] as MutableMap<String, Any?>)["meta"] = meta
            }
            if (type == "t") {
                val credential = credentials?.get(0)?.asJsonObject
                credential?.getStringOrNull("is")
                    ?.let { i -> payloadMap["issuer"] = mapOf("name" to i) }
                payloadMap["cardArt"] = SPAY_COMPANY_TEST_CARD_ART
                payloadMap["type"] = "TestResultCard"
                payloadMap["types"] = setOf("TestResultCard")
                payloadMap["testResult"] = listOf(mapOf(
                    "result" to valueMapper["test-result-dcc"]?.getOrDefault(
                        credential?.getStringOrNull("tr"),
                        "Positive"
                    ),
                    "disease" to valueMapper["disease-agent-targeted-dcc"]?.getOrDefault(
                        credential?.getStringOrNull("tg"),
                        "COVID-19"
                    ),
                    "date" to credential?.getStringOrNull("sc")
                        ?.toDate("yyyy-MM-dd", false)
                        ?.format(SPAY_DATE_FORMAT)
                ))
            }
        }
        return gsonBuilder.toJson(payloadMap).toString()
    }

    private fun dhpSchema(it: VerifiableObject): String {
        val payload = it.credential
        val credential = payload?.credentialSubject
        val recipient = credential?.getAsJsonObject("recipient")
        val issuer = getCredentialAndSchema()?.let {
            it.getIssuerName().ifEmpty { "Unknown Issuer" }
        }
        val credentialType = credential?.getStringOrNull("type")
        val payloadMap = mutableMapOf(
            "version" to SPAY_SCHEMA_VERSION,
            "cardId" to payload?.id,
            "qrData" to mapOf(
                "decoding" to "None",
                "chunks" to listOf(it.rawString)
            ),
            "expirationDate" to payload?.expirationDate
                ?.toDate(dateFormat = SERVER_DATE_FORMAT, false)
                ?.format(SPAY_DATE_FORMAT),
            "provider" to mapOf("name" to SPAY_PROVIDER_NAME),
            "issuer" to mapOf("name" to issuer),
            "patient" to mapOf(
                "dateOfBirth" to recipient
                    ?.getStringOrNull("birthDate")
                    ?.replace("-", ""),
                "name" to mapOf(
                    "family" to recipient?.getStringOrNull("familyName"),
                    "given" to recipient?.getStringOrNull("givenName")
                )
            )
        )
        when (credentialType) {
            "Vaccination Card" -> {
                payloadMap["cardArt"] = SPAY_COMPANY_VACCINE_CARD_ART
                payloadMap["type"] = "VaccinationRecordCard"
                payloadMap["types"] = setOf("VaccinationRecordCard")
                payloadMap["vaccinations"] = listOf(
                    mapOf(
                        "vaccine" to mapOf(
                            "product" to credential.let {
                                if (it.has("medicinalProductName")) {
                                    it.getStringOrNull("medicinalProductName")
                                } else {
                                    if (it.has("marketingAuthorizationHolder"))
                                        it.getStringOrNull("marketingAuthorizationHolder")
                                    else credential.getStringOrNull("cvxCode")?.let { code ->
                                        convertCodeToBrandName(code)
                                    }
                                }
                            },
                            "date" to credential.getStringOrNull("dateOfVaccination")
                                ?.replace("-", "")
                        )
                    )
                )
            }
            "Test Results" -> {
                payloadMap["cardArt"] = SPAY_COMPANY_TEST_CARD_ART
                payloadMap["type"] = "TestResultCard"
                payloadMap["types"] = setOf("TestResultCard")
                payloadMap["testResult"] = listOf(mapOf(
                    "result" to credential.getStringOrNull("testResult"),
                    "disease" to credential.getStringOrNull("disease"),
                    "date" to credential.getStringOrNull("dateOfSample")
                        ?.toDate("yyyy-MM-dd", false)
                        ?.format(SPAY_DATE_FORMAT),
                ))
            }
        }
        return gsonBuilder.toJson(payloadMap).toString()
    }

    private fun shcSchema(it: VerifiableObject): String {
        val issuer = getCredentialAndSchema()?.let {
            it.getIssuerName().ifEmpty { "Unknown Issuer" }
        }
        val payloadMap = mutableMapOf(
            "version" to SPAY_SCHEMA_VERSION,
            "cardId" to it.rawString,
            "qrData" to mapOf(
                "decoding" to "None",
                "chunks" to listOf(it.rawString)
            ),
            "expirationDate" to it.jws?.payload?.exp?.toLong()?.let { TimeUnit.SECONDS.toMillis(it) }
                ?.timestampToDate()
                ?.format(SPAY_DATE_FORMAT),
            "provider" to mapOf("name" to SPAY_PROVIDER_NAME),
            "issuer" to mapOf("name" to issuer)
        )
        it.jws?.payload?.vc?.credentialSubject?.getAsJsonObject("fhirBundle")
            ?.getAsJsonArray("entry")?.forEach {
                val resource = (it as JsonObject).getAsJsonObject("resource")
                when (resource.getStringOrNull("resourceType")?.toLowerCase()) {
                    "patient" -> {
                        val name = resource.getAsJsonArray("name")[0] as JsonObject
                        payloadMap["patient"] = mapOf(
                            "dateOfBirth" to resource.getStringOrNull("birthDate")
                                ?.replace("-", ""),
                            "name" to mapOf(
                                "family" to name.getStringOrNull("family"),
                                "given" to name.getListOrNull<String>("given")
                                    ?.joinToString(separator = " ")
                            )
                        )
                    }
                    "immunization" -> {
                        payloadMap["cardArt"] = SPAY_COMPANY_VACCINE_CARD_ART
                        payloadMap["type"] = "VaccinationRecordCard"
                        (payloadMap.getOrPut("types") { mutableSetOf<String>() } as MutableSet<String>)
                            .add("VaccinationRecordCard")
                        val vaccineBrandCode = (resource
                            .getAsJsonObject("vaccineCode")
                            .getAsJsonArray("coding")[0] as JsonObject)
                            .getStringOrNull("code")
                        val vaccination = mapOf(
                            "vaccine" to mapOf(
                                "product" to vaccineBrandCode?.let { code ->
                                    convertCodeToBrandName(
                                        code
                                    )
                                },
                                "lot" to resource.getStringOrNull("lotNumber"),
                                "date" to resource.getStringOrNull("occurrenceDateTime")
                                    ?.replace("-", "")
                            ),
                            "performer" to mapOf(
                                "name" to resource.getAsJsonArray("performer")
                                    ?.get(0)
                                    ?.asJsonObject
                                    ?.getAsJsonObject("actor")
                                    ?.getStringOrNull("display")
                                /*"identityAssuranceLevel" to "IAL1.2"*/
                            )
                        )
                        if (!payloadMap.containsKey("vaccinations")) {
                            payloadMap["vaccinations"] = ArrayList<Any?>()
                        }
                        (payloadMap["vaccinations"] as ArrayList<Any?>).add(vaccination)
                    }
                    "observation" -> {
                        payloadMap["cardArt"] = SPAY_COMPANY_TEST_CARD_ART
                        payloadMap["type"] = "TestResultCard"
                        (payloadMap.getOrPut("types") { mutableSetOf<String>() } as MutableSet<String>)
                            .add("TestResultCard")
                        val resultCode = (resource.getAsJsonObject("valueCodeableConcept")
                            .getAsJsonArray("coding")[0] as JsonObject).getStringOrNull("code")
                        val diseaseCode = (resource.getAsJsonObject("code")
                            .getAsJsonArray("coding")[0] as JsonObject).getStringOrNull("code")
                        val testResult = listOf(mapOf(
                            "result" to valueMapper["test-result-dcc"]?.getOrDefault(resultCode, "Positive"),
                            "disease" to diseaseCode,
                            "date" to resource.getStringOrNull("effectiveDateTime")
                                ?.toDate("yyyy-MM-dd", false)
                                ?.format(SPAY_DATE_FORMAT)
                        ))
                        payloadMap["testResult"] = testResult
                    }
                    else -> {
                        //THROW error
                        Toast.makeText(context, "Card type not supported", Toast.LENGTH_SHORT)
                            .show()
                        return "ERROR"
                    }
                }
            }
        return gsonBuilder.toJson(payloadMap).toString()
    }

    private fun convertCodeToBrandName(vaccineCode: String): String {
        return when (vaccineCode) {
            "208" -> "Pfizer/BioNTech"
            "207" -> "Moderna"
            "210" -> "AstraZeneca"
            "212" -> "Janssen/Johnson & Johnson"
            else -> return vaccineCode
        }
    }

    private fun showCredentialSource() {
        val nav = findNavController()
        if (nav.currentDestination?.id == R.id.credential_details_fragment) {
            getCredentialAndSchema()?.verifiableObject?.let {
                val json = when (it.type) {
                    VCType.GHP, VCType.IDHP, VCType.VC, VCType.DIVOC ->
                        it.credential?.credentialSubject
                    VCType.SHC -> it.jws?.payload?.vc?.credentialSubject
                    VCType.DCC -> it.cose?.hCertJson?.toJsonElement().asJsonObjectOrNull()
                    else -> EMPTY_JSON.toJsonElement().asJsonObjectOrNull()
                }

                val credentialJsonString = GsonBuilder().setPrettyPrinting().create().toJson(json)
                val bundle =
                    bundleOf(CredentialSourceFragment.KEY_CREDENTIAL to credentialJsonString)
                nav.navigate(R.id.action_credential_source, bundle)
            }
        }
    }

    private fun showObfuscationSettings(key: Int) {
        getCredentialAndSchema()?.let { cp ->
            val bundle =
                bundleOf(
                    ObfuscationSettingsFragment.KEY_CREDENTIAL_PACKAGE to cp,
                    ObfuscationSettingsFragment.KEY_MENU_FUNCTION to key
                )
            findNavController().navigate(
                R.id.action_credential_details_to_obfuscation_fragment,
                bundle
            )
        }
    }

    private fun showFullScreen(aPackage: Package) {
        val bundle = bundleOf(FullScreenQRFragment.KEY_CREDENTIAL_PACKAGE to aPackage)
        findNavController().navigate(R.id.action_credential_details_to_fullscreen, bundle)
    }

    private fun loadData() {
        var isAvailable = false
        contactsSection = ContactsSection()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            contactsSection.tag = getString(R.string.connections)

            adapter = connectionSectionAdapter
        }
        connectionSectionAdapter.removeAllSections()
        loadingDisposable.add(
            profileCredId?.let {
                viewModel.loadConnections()
                    .subscribe({
                        if (it.isNotEmpty()) {
                            connectionSectionAdapter?.addIfNotExist(contactsSection)
                            it.forEach {
                                it.uploadedCredentialList.forEach {
                                    if (it.credentialId != null) {
                                        if (it.credentialId.equals(profileCredId)) {
                                            isAvailable = true
                                        }
                                    }
                                }
                                if (isAvailable) {
                                    contactsSection.addItem(it)
                                    isAvailable = false
                                }
                            }
                            if (contactsSection.contentItemsTotal > 0) {
                                connectionSectionAdapter?.getAdapterForSection(contactsSection)
                                    ?.notifyAllItemsChanged()
                            } else {
                                connectionSectionAdapter?.removeSection(contactsSection)
                            }
                        } else {
                            connectionSectionAdapter?.removeSection(contactsSection)
                        }

                        updateConnectionsVisibility()
                    }, rxError("failed to load data"))
            }
        )
    }

    private fun updateConnectionsVisibility() {
        val isAdapterEmpty = connectionSectionAdapter.itemCount == 0
        if (viewModel.getEnvironment().getEnvironmentTitle(resources).contains("USA")) {
            binding.recyclerView.isVisible = !isAdapterEmpty
        }
    }

    private fun showDeleteAlertDialog() {
        activity?.showDialog(
            getString(R.string.cred_delete_title),
            getString(R.string.delete_credentials_message),
            getString(R.string.kpm_delete_confirm), { _ ->
                getCredentialAndSchema()?.let {
                    deleteDisposable.set(
                        viewModel.deleteCredential(it)
                            .subscribe({
                                goToWallet()
                            }, rxError("failed to delete"))
                    )
                }
            },
            getString(R.string.button_title_cancel), {}
        )
    }

    private fun setUpSamsungPay(firstTimeCalled: Boolean) {

        val bundle = Bundle()
        bundle.putString(
            SamsungPay.PARTNER_SERVICE_TYPE,
            SpaySdk.ServiceType.APP2APP.toString()
        )
        val pInfo = PartnerInfo(BuildConfig.SPAY_SERVICEID, bundle)
        val samsungPay = SamsungPay(context, pInfo)

        samsungPay.getSamsungPayStatus(object : StatusListener {
            override fun onSuccess(status: Int, bundle: Bundle?) {
                when (status) {
                    SamsungPay.SPAY_NOT_SUPPORTED -> {
                        binding.addSamsungPayBtn.visibility = View.GONE
                    }
                    SamsungPay.SPAY_NOT_READY -> {
                        when (val extraReason = bundle!!.getInt(SamsungPay.EXTRA_ERROR_REASON)) {
                            SamsungPay.ERROR_SPAY_APP_NEED_TO_UPDATE -> {
                                if (!firstTimeCalled) samsungPay.goToUpdatePage()
                                else isCredentialSupported()
                            }
                            SamsungPay.ERROR_SPAY_SETUP_NOT_COMPLETED -> {
                                if (!firstTimeCalled) samsungPay.activateSamsungPay()
                                else isCredentialSupported()
                            }
                            else -> {
                                binding.addSamsungPayBtn.visibility = View.GONE
                                Log.e(TAG, "Samsung PAY is not ready, extra reason: $extraReason")
                            }
                        }
                    }
                    SamsungPay.SPAY_READY -> {
                        if (firstTimeCalled) isCredentialSupported()
                        else addCredentialToSamsungPay()
                    }
                    else -> {
                        //Something went wrong
                        binding.addSamsungPayBtn.visibility = View.GONE
                    }
                }
            }

            override fun onFail(errorCode: Int, bundle: Bundle?) {
                binding.addSamsungPayBtn.visibility = View.GONE
                Log.e(TAG, "checkSamsungPayStatus onFail() : $errorCode")
            }
        })
    }

    private fun isCredentialSupported() {
        getCredentialAndSchema()?.verifiableObject?.let {
            when (it.type) {
                VCType.GHP, VCType.IDHP, VCType.VC -> {
                    val type = it.credential?.credentialSubject?.asJsonObject?.getStringOrNull("type")
                    if (it.credential?.hasObfuscation() != true && type?.let { t ->
                            dhpSupportedTypes.any { s -> t.toLowerCase().contains(s) }
                        } == true
                    ) {
                        CoroutineScope(Dispatchers.IO).launch {
                            checkCountryCode(it.isExpired())
                        }
                    } else binding.addSamsungPayBtn.visibility = View.GONE
                }
                VCType.DCC -> {
                    val type = it.cose?.hCertJson?.toJsonElement().asJsonObjectOrNull()
                    val vax = type?.getAsJsonArray("v") //Check if it contains a vaccine
                    val test = type?.getAsJsonArray("t")//Check if it contains a test
                    if (vax != null || test != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            checkCountryCode(it.isExpired())
                        }
                    } else binding.addSamsungPayBtn.visibility = View.GONE
                }
                VCType.SHC -> {
                    val entries = it.jws?.payload?.vc?.credentialSubject?.getAsJsonObject("fhirBundle")
                        ?.getAsJsonArray("entry")
                    if (entries != null) {
                        if (entries.any { e ->
                                val resource = (e as JsonObject).getAsJsonObject("resource")
                                val resourceType = resource
                                    .getStringOrNull("resourceType")
                                    ?.toLowerCase()
                                resourceType != null && shcSupportedTypes.contains(resourceType.toLowerCase())
                            }){
                            CoroutineScope(Dispatchers.IO).launch {
                                checkCountryCode(it.isExpired())
                            }
                        }
                        else binding.addSamsungPayBtn.visibility = View.GONE
                    }
                    else binding.addSamsungPayBtn.visibility = View.GONE
                }
                else -> binding.addSamsungPayBtn.visibility = View.GONE
            }
        }
    }


    private suspend fun checkCountryCode(isExpired: Boolean) {

        val countryCode = viewModel.getCountryCode()
        val model = viewModel.getModelName()
        val deviceIsOnline = context?.isInternetAvailable()

        if (deviceIsOnline == true) Log.d(TAG, "Device is online")
        else Log.e(TAG, "Device is OFFLINE")

        Log.d(TAG, "Country Code: $countryCode")
        Log.d(TAG, "Model Number: $model")

        CoroutineScope(Dispatchers.IO).launch {
            if (countryCode != null && model != null && deviceIsOnline == true) {
                val isAvailable = viewModel.checkDeviceAvailSamsungPay(countryCode, model, "WALLET")
                if (isAvailable == true) {
                    withContext(Main) {
                        Log.d(TAG, "Device is available")
                        if (activity != null && !isExpired) binding.addSamsungPayBtn.visibility = View.VISIBLE
                    }
                } else {
                    withContext(Main) {
                        Log.e(TAG, "Device is NOT available")
                        if (activity != null) binding.addSamsungPayBtn.visibility = View.GONE
                    }
                }
            } else {
                withContext(Main) {
                    Log.e(TAG, "Country Code or Model Number is null, or device is offline")
                    if (activity != null) binding.addSamsungPayBtn.visibility = View.GONE
                }
            }
        }
    }

    private fun initUi(view: View) {
        getCredentialAndSchema()?.let { cp ->
            //add a way to update the credential but only for dev mode
            if (BuildConfig.DEBUG) {
                binding.credDetailsCardContainer.walletNameTextview.setOnLongClickListener {
                    updateCredentials(cp)
                    true
                }
            }

            ViewUtils.adjustCredentialView(view, cp)
            val rawString = cp.verifiableObject.rawString
            verifiableObject = cp.verifiableObject
            profileCredId = fetchCredentialID(cp.verifiableObject)

            ViewUtils.adjustCredentialView(view, cp)

            binding.credDetailsBarcodeImageview.setImageFromQR(rawString)
            binding.credDetailsBarcodeImageview.setOnClickListener {
                //TODO Add these back when obfuscation story comes back
//                if (credential.hasObfuscation()) {
//                    showObfuscationSettings(ObfuscationSettingsFragment.TO_QR)
//                } else {
                showFullScreen(cp)
//                }
            }


            val fieldsList = when (cp.verifiableObject.type) {
                VCType.SHC -> {
                    DisplayFieldProcessor(loadFieldConfigurationJson("display_shc.json", "SHC"))
                        .getDisplayFields(cp.verifiableObject, valueMapper)
                        .sortedBy(Field::sectionIndex)
                }
                VCType.DCC -> {
                    DisplayFieldProcessor(loadFieldConfigurationJson("display_dcc.json", "DCC"))
                        .getDisplayFields(cp.verifiableObject, valueMapper)
                        .sortedBy(Field::sectionIndex)
                }
                else -> {
                    SchemaProcessor().processSchemaAndSubject(cp)
                }
            }

            val filteredList = fieldsList.filter { field -> field.visible }.toArrayList()
            binding.rvCredDetails.isVisible = filteredList.isNotEmpty()

            binding.rvCredDetails.layoutManager = LinearLayoutManager(context)
            binding.rvCredDetails.setHasFixedSize(true)

            sectionAdapter.removeAllSections()
            filteredList.forEach { field ->
                var header =
                    if (cp.verifiableObject.type == VCType.DCC || cp.verifiableObject.type == VCType.SHC) {
                        field.getSectionHeader(Locale.getDefault())
                    } else {
                        field.getFirstPathPart(getString(R.string.details)).capitalize()
                    }
                if (header.isEmpty()) {
                    header = field.sectionIndex.toString()
                }

                val section = DetailsAdapter(header, !header.isDigitsOnly())
                val loadedSection = sectionAdapter.getSection(header)

                val parsedFieldValue = when (field.value) {
                    is JsonArray -> {
                        parseMultilineString(field.value)
                    }
                    else -> field.value
                }

                val updatedFieldValue = when (parsedFieldValue) {
                    is List<*> -> {
                        parsedFieldValue.reduce { it, str -> "$it : ${str}" }
                    }
                    else -> parsedFieldValue
                }

                if (updatedFieldValue != null) {
                    field.value = updatedFieldValue
                }

                if (loadedSection == null) {
                    section.addItem(field)
                    sectionAdapter.addSection(header, section)
                } else {
                    (loadedSection as DetailsAdapter).addItem(field)
                }
            }
            binding.rvCredDetails.adapter = sectionAdapter

            loadCardsInfo(cp.verifiableObject)
        }
    }

    private fun loadCardsInfo(verifiableObject: VerifiableObject) {
        binding.cardInfoRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.cardInfoRecyclerView.setHasFixedSize(true)
        if (verifiableObject.issuanceDate.isNotNullOrEmpty() || verifiableObject.expiryDate.isNotNullOrEmpty()) {
            val header = getString(R.string.card_info)
            val section = DetailsAdapter(header, header.length > 1 || !header.isDigitsOnly())

            if (verifiableObject.expiryDate.isNotNullOrEmpty()) {
                val expiryDateField = if (verifiableObject.isExpired()) {
                    Field.create(
                        getString(R.string.result_expiredDate).toLowerCase(),
                        verifiableObject.expiryDate
                    )
                } else {
                    Field.create(
                        getString(R.string.result_expiresDate).toLowerCase(),
                        verifiableObject.expiryDate
                    )
                }

                section.addItem(expiryDateField)
            }
            if (verifiableObject.issuanceDate.isNotNullOrEmpty()) {
                val issuedDateField = Field.create(
                    getString(R.string.result_issuedDate).toLowerCase(),
                    verifiableObject.issuanceDate
                )

                section.addItem(issuedDateField)
            }
            cardsInfoSectionAdapter.addSection(header, section)
        }
        binding.cardInfoRecyclerView.adapter = cardsInfoSectionAdapter

        loadRecordVerificationInfo(verifiableObject)
    }

    private fun loadRecordVerificationInfo(verifiableObject: VerifiableObject) {
        verifiableEngine = VerifyEngine(verifiableObject)

        resultsModel.verifiableObject = verifiableObject
        resultsModel.verifyEngine = verifiableEngine
        showLoading(true)
        viewModel.validate(resultsModel)
            .doFinally { showLoading(false) }
            .asyncToUiSingle()
            .subscribe({
                adjustSignatureStatus(it)
            }, {
                it.printStackTrace()
            })
            .addTo(disposables)
    }

    private fun adjustSignatureStatus(results: VerificationResultsModel) {
        val header = getString(R.string.record_verification)
        val section = DetailsAdapter(header)
        val signatureSectionItem: Field = when {
            results.isSignVerified -> {
                Field.create(
                    getString(R.string.signature).toLowerCase(),
                    getString(R.string.result_Verified)
                )
            }
            else -> {
                Field.create(
                    getString(R.string.signature).toLowerCase(),
                    getString(R.string.result_notVerified) + " -\n" +
                            resultsModel.result?.messageResId?.let { getString(it) }
                )
            }
        }
        signatureSectionItem.recordStatus = results.isSignVerified.toString()
        section.addItem(signatureSectionItem)

        when (results.verifiableObject?.type) {
            VCType.IDHP, VCType.GHP, VCType.VC -> {
                val statusSectionItem: Any?
                statusSectionItem = when {
                    results.isNotRevoked -> {
                        Field.create(
                            getString(R.string.status).toLowerCase(),
                            getString(R.string.status_not_revoked)
                        )
                    }
                    else -> {
                        Field.create(
                            getString(R.string.status).toLowerCase(),
                            getString(R.string.status_revoked)
                        )
                    }
                }
                statusSectionItem.recordStatus = results.isNotRevoked.toString()
                section.addItem(statusSectionItem)
            }
            else -> {
            }
        }
        cardsInfoSectionAdapter.addSection(header, section)
        cardsInfoSectionAdapter.notifyDataSetChanged()
    }

    private fun parseMultilineString(fieldValue: Any): String {
        val parsedString = StringBuilder()
        val jsonValue = (fieldValue as JsonArray)
        for (i in 0 until jsonValue.size()) {
            if (i < jsonValue.size() - 1)
                parsedString.appendLine(jsonValue[i].toString())
            else
                parsedString.append(jsonValue[i].toString())
        }
        return parsedString.toString().replace("\"", "")
    }

    private fun loadFieldConfigurationJson(assetName: String, type: String) =
        context?.loadAssetsFileAsString(assetName)
            ?.toJSONObject()
            ?.getJSONObject("configuration")
            ?.getJSONObject(type)
            ?.getJSONArray("details")
            ?.toString()
            .orValue("{}")

    private fun loadValueMapperJson() =
        context?.loadAssetsFileAsString("value-mapper.json")
            ?.toJSONObject()
            ?.toString()
            .orValue("{}")

    override fun onResume() {
        super.onResume()
        applySupportActionBar {
            title = getCredentialAndSchema()?.getSchemaName()
        }
    }

    override fun onDestroy() {
        RxHelper.unsubscribe(deleteDisposable.get(), updateDisposable.get(), loadingDisposable)
        super.onDestroy()
    }

    //region menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menus_cred_details, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_share -> {
                getCredentialAndSchema()?.let {
                    if (it.verifiableObject.credential!!.hasObfuscation()) {
                        showObfuscationSettings(ObfuscationSettingsFragment.TO_SHARE)
                    } else {
                        getCredentialAndSchema()?.let { cp -> showShareCredentialDialog(cp) }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
//endregion

    private fun updateCredentials(aPackage: Package) {
        if (BuildConfig.DEBUG) {
            updateDisposable.set(
                viewModel.updateCredential(aPackage)
                    .subscribe({
                        view?.snackShort("dev feature, updated: $it")
                        view?.let { view -> initUi(view) }
                    }, rxError("could not update"))
            )
        }
    }

    private fun fetchCredentialID(verifiableObject: VerifiableObject): String? {
        return when {
            verifiableObject.isIDHPorGHPorVC -> verifiableObject.credential?.id
            verifiableObject.type == VCType.SHC -> verifiableObject.jws?.payload?.nbf?.toBigDecimal()
                ?.toPlainString()
            verifiableObject.type == VCType.DCC -> verifiableObject.cose?.cwt?.let { getDgci(it) }
            else -> null
        }
    }

    private fun getDgci(cwt: CWT): String? {
        return try {
            return when (true) {
                cwt.certificate?.vaccinations?.isNotEmpty() == true -> cwt.certificate?.vaccinations?.last()?.certificateIdentifier
                cwt.certificate?.tests?.isNotEmpty() == true -> cwt.certificate?.tests?.last()?.certificateIdentifier
                cwt.certificate?.recoveryStatements?.isNotEmpty() == true -> cwt.certificate?.recoveryStatements?.last()?.certificateIdentifier
                else -> ""
            }
        } catch (ex: Exception) {
            ""
        }
    }

    private fun getReason(throwable: Throwable): String {
        var message = throwable.localizedMessage.orValue("Unknown Error")
        when {
            (throwable is VerifyError.CredentialSignatureNoProperties) -> {
                message =
                    getText(R.string.verification_invalidSignature_notFound) as String
            }
            (throwable is VerifyError.CredentialSignatureUnsupportedKey) -> {
                message =
                    getText(R.string.verification_invalidSignature_unsupportedKeys) as String
            }
            (throwable is VerifyError.CredentialSignatureInvalidSignatureData) -> {
                message =
                    getText(R.string.verification_invalidSignature_invalidSignatureData) as String
            }
        }

        return message
    }

    override fun onDestroyView() {
        RxHelper.unsubscribe(disposables)
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_CREDENTIAL_PACKAGE = "credentials_package"
        const val REQUEST_CODE_SHARE = 1
        const val EMPTY_JSON = "{}"
        const val SPAY_SCHEMA_VERSION = "1.5"
        const val SPAY_DATE_FORMAT = "yyyyMMdd"
        const val SPAY_COMPANY_VACCINE_CARD_ART =
            "https://raw.githubusercontent.com/IBM/digital-health-pass/main/DHP_samsung_pay_vaccination_card.png"
        const val SPAY_COMPANY_TEST_CARD_ART =
            "https://raw.githubusercontent.com/IBM/digital-health-pass/main/DHP_samsung_pay_test_card.png"
        const val SPAY_PROVIDER_NAME = "Open Digital Health Pass Wallet"
    }
}