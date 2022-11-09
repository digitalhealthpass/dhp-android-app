package com.merative.healthpass.ui.contactDetails.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragmentContactDetailsBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.contactDetails.associatedData.AssociatedDataDetailsFragment.Companion.KEY_PACKAGE
import com.merative.healthpass.ui.contactDetails.asymmetricKey.AsymmetricKeyFragment
import com.merative.healthpass.ui.contactDetails.consent.ConsentFragment
import com.merative.healthpass.ui.contactDetails.revoke.ConsentRevokeFragment
import com.merative.healthpass.ui.credential.details.CredentialDetailsFragment
import com.merative.healthpass.ui.scanVerify.ScanVerifyFragment
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.schema.SchemaProcessor
import com.merative.watson.healthpass.verifiablecredential.extensions.getStringOrNull
import com.merative.watson.healthpass.verifiablecredential.models.credential.getFirstPiiController
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import java.util.*
import kotlin.collections.ArrayList

class ContactDetailsFragment : BaseFragment() {

    private var _binding: FragmentContactDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ContactDetailsVM
    private lateinit var contactPack: ContactPackage
    private val deleteDisposable = SerialDisposable()
    private val clickDisposable = CompositeDisposable()
    private val loadingDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactPack = requireArguments().get(KEY_CONTACT_PACKAGE) as ContactPackage
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentContactDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(ContactDetailsVM::class.java)
        initUI()
    }

    private fun initUI() {
        binding.cdImageview.setImageFromBase64(
            contactPack.profilePackage?.issuerMetaData?.metadata?.logo,
            contactPack.getContactName().getInitials()
        )

        binding.cdDownloadLayoutHeader.isVisible = contactPack.canDownload()
        binding.cdDownloadedPassesRv.isVisible = contactPack.canDownload()
        if (contactPack.canDownload()) {
            loadDownloadedCredsRecycler()
        }

        binding.cdUploadLayoutHeader.isVisible = contactPack.canUpload()
        binding.cdUploadedPassesRv.isVisible = contactPack.canUpload()
        if (contactPack.canUpload()) {
            loadUploadedCredsRecycler()
        }

        if (contactPack.isDefaultPackage()) {
            initDefault()
        } else {
            initCustom()
        }

        binding.cdDeleteContactButton.setOnClickListener {
            delete()
        }

        binding.cdAssociatedKeyButton.apply {
            binding.cdAssociatedKeyButton.isEnabledAlpha = contactPack.asymmetricKey != null

            setOnClickListener {
                findNavController().navigate(
                    R.id.contactAssociatedKeyFragment,
                    bundleOf(
                        AsymmetricKeyFragment.KEY_PACKAGE_KEY to contactPack
                    )
                )
            }
        }

        binding.cdAssociatedDataButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_contactDetailsFragment_to_contactAssociatedDataFragment,
                bundleOf(KEY_PACKAGE to contactPack)
            )
        }

        if (arguments?.getBoolean(KEY_DOWNLOAD) == true) {
            downloadCredentials()
        }
    }

    private fun initCustom() {
        val profilePiiController = contactPack.profilePackage?.verifiableObject?.credential?.getFirstPiiController()
        val idPackageCredentialSubject = contactPack.idPackage?.verifiableObject?.credential?.credentialSubject

        //Setting Header Name and Sub header
        binding.cdContactNameTextview.text =
            profilePiiController?.getStringOrNull("piiController").orEmpty()


        binding.cdContactNameSubheaderTextview.text = getString(R.string.contact)

        binding.cdCallButton.apply {
            isEnabledAlpha = profilePiiController?.getStringOrNull("phone").isNotNullOrEmpty()
            setOnClickListener {
                activity?.openDialer(profilePiiController?.getStringOrNull("phone").orEmpty())
            }
        }


        binding.cdEmailButton.apply {
            isEnabledAlpha = profilePiiController?.getStringOrNull("email").isNotNullOrEmpty()
            setOnClickListener {
                activity?.sendEmail(
                    profilePiiController?.getStringOrNull("email").orEmpty(),
                    "",
                    ""
                )
            }
        }


        binding.cdWebsiteButton.apply {
            isEnabledAlpha =
                profilePiiController?.getStringOrNull("piiControllerUrl").isNotNullOrEmpty()
            setOnClickListener {
                requireActivity().openUrl(
                    profilePiiController?.getStringOrNull("piiControllerUrl").orEmpty()
                )
            }
        }

        adjustNihRecycler(profilePiiController, idPackageCredentialSubject)
    }

    private fun initDefault() {
        val profileCredSub = contactPack.profilePackage?.verifiableObject?.credential?.credentialSubject
        val idCredSub = contactPack.idPackage?.verifiableObject?.credential?.credentialSubject

        //Setting Header Name and Sub header

        binding.cdContactNameTextview.text = profileCredSub?.getStringOrNull("name").orEmpty()
        binding.cdContactNameSubheaderTextview.text =
            idCredSub?.getStringOrNull("location").orEmpty()

        binding.cdCallButton.isEnabledAlpha = false

        binding.cdEmailButton.apply {
            isEnabledAlpha = profileCredSub?.getStringOrNull("contact").isNotNullOrEmpty()
            setOnClickListener {
                activity?.sendEmail(
                    profileCredSub?.getStringOrNull("contact").orEmpty(),
                    "",
                    ""
                )
            }
        }

        binding.cdWebsiteButton.apply {
            isEnabledAlpha =
                profileCredSub?.getStringOrNull("website").isNotNullOrEmpty()
            setOnClickListener {
                requireActivity().openUrl(
                    profileCredSub?.getStringOrNull("website").orEmpty()
                )
            }
        }

        adjustOtherRecycler(profileCredSub, idCredSub)
    }

    private fun delete() {
        if (contactPack.canUpload()) {
            val bundle = bundleOf(
                ConsentRevokeFragment.KEY_CONTACT to contactPack
            )
            findNavController().navigate(
                R.id.action_contactDetailsFragment_to_consentRevokeFragment,
                bundle
            )
        } else {
            activity?.showDialog(null,
                getString(R.string.dialog_delete_connection_msg),
                getString(R.string.contact_delete_title),
                {
                    showLoading(true)
                    deleteDisposable.set(
                        viewModel.deleteContact(contactPack)
                            .subscribe(
                                {
                                    if (!it.isSuccessful) {
                                        showLoading(false)
                                        handleCommonErrors(it)
                                        deleteAnyway()
                                    }
                                },
                                {
                                    showLoading(false)
                                    handleCommonErrors(it)
                                    deleteAnyway()
                                },
                                {
                                    showLoading(false)
                                    goToWallet()
                                }
                            )
                    )
                },
                getString(R.string.button_title_cancel),
                {})
        }
    }

    private fun deleteAnyway() {
        activity?.showDialog(
            getString(R.string.contact_offboardingFailed_title),
            getString(R.string.contact_offboardingFailed_message),
            getString(R.string.contact_offboardingFailed_button2),
            {
                deleteDisposable.set(
                    viewModel.deleteContactDB(contactPack)
                        .subscribe {
                            goToWallet()
                        }
                )
            },
            getString(R.string.button_title_cancel),
            {}
        )
    }

    private fun adjustNihRecycler(
        profilePiiController: JsonObject?,
        idPackageCredentialSubject: JsonObject?
    ) {
        val profileAdapter = ContactPairAdapter()

        binding.cdFirstRv.layoutManager = LinearLayoutManager(requireContext())
        binding.cdFirstRv.adapter = profileAdapter
        val profileDataList = ArrayList<Pair<String, CharSequence>>()
        profileDataList.add(
            getString(R.string.contact_name) to profilePiiController?.getStringOrNull("contact")
                .orEmpty()
        )
        profileDataList.add(
            getString(R.string.phone) to profilePiiController?.getStringOrNull("phone").orEmpty()
        )
        profileDataList.add(
            getString(R.string.contact_email) to profilePiiController?.getStringOrNull("email")
                .orEmpty()
        )
        profileDataList.add(
            getString(R.string.contact_address) to
                    profilePiiController?.getAsJsonObject("address")?.getFormattedAddress()
                        .orValue("")
        )
        profileDataList.add(
            getString(R.string.contact_website) to profilePiiController?.getStringOrNull("piiControllerUrl")
                .orEmpty()
        )
        profileAdapter.addItems(profileDataList)
        profileAdapter.notifyDataSetChanged()

        // Registration or ID Section
        val idAdapter = ContactPairAdapter()
        binding.cdSecondRv.layoutManager = LinearLayoutManager(requireContext())
        binding.cdSecondRv.adapter = idAdapter
        val idDataList = ArrayList<Pair<String, CharSequence>>()
        idDataList.add(
            getString(R.string.contact_gender) to idPackageCredentialSubject?.getStringOrNull("gender")
                .orEmpty()
        )
        idDataList.add(
            getString(R.string.contact_age) to idPackageCredentialSubject?.getStringOrNull("ageRange")
                .orEmpty()
        )
        idDataList.add(
            getString(R.string.contact_race) to
                    idPackageCredentialSubject?.getAsJsonArray("race")?.toList()
                        .toStringWithoutBrackets()
                        .removeExtraQuote()
        )
        idDataList.add(
            getString(R.string.contact_location) to idPackageCredentialSubject?.getStringOrNull("location")
                .orEmpty()
        )
        idAdapter.setItems(idDataList)
    }

    private fun adjustOtherRecycler(
        profileCredSub: JsonObject?,
        idCredSub: JsonObject?
    ) {
        //registration rv
        val registrationAdapter = ContactPairAdapter()
        binding.cdFirstRv.layoutManager = LinearLayoutManager(requireContext())
        binding.cdFirstRv.adapter = registrationAdapter
        val regFieldsList = contactPack.idPackage?.let {
            SchemaProcessor().processSchemaAndSubject(it)
        }
        val filteredRegFieldList = regFieldsList
            ?.filter { field -> field.visible }
            .toArrayList()
        if (filteredRegFieldList.isEmpty()) {
            binding.cdFirstRvHeader.visibility = View.GONE
            binding.cdFirstRv.visibility = View.GONE
        } else {
            val regDataList = ArrayList<Pair<String, String>>()
            for (field in filteredRegFieldList) {
                regDataList.add(field.getUsableValue(Locale.getDefault()))
            }
            registrationAdapter.addItems(regDataList)
            registrationAdapter.notifyDataSetChanged()
        }

        //contact info rv
        val contactInfoAdapter = ContactPairAdapter()
        binding.cdSecondRv.layoutManager = LinearLayoutManager(requireContext())
        binding.cdSecondRv.adapter = contactInfoAdapter
        val ciFieldsList = contactPack.profilePackage?.let {
            SchemaProcessor().processSchemaAndSubject(it)
        }
        val filteredCiFieldList = ciFieldsList
            ?.filter { field -> field.visible }
            .toArrayList()
        if (filteredCiFieldList.isEmpty()) {
            binding.cdSecondRvHeader.visibility = View.GONE
            binding.cdSecondRv.visibility = View.GONE
        } else {
            val ciDataList = ArrayList<Pair<String, CharSequence>>()
            for (field in filteredCiFieldList) {
                ciDataList.add(field.getUsableValue(Locale.getDefault()))
            }
            contactInfoAdapter.setItems(ciDataList)
        }
    }

    private fun loadDownloadedCredsRecycler() {
        binding.cdDownloadedPassesRv.layoutManager = LinearLayoutManager(requireContext())
        val section = UDCredentialSection(
            R.string.cd_download_hp,
            R.drawable.ic_cloud_download,
            viewModel.shouldDisableFeatureForRegion()
        )
        val adapter = SectionedRecyclerViewAdapter()
        binding.cdDownloadedPassesRv.adapter = adapter

        loadingDisposable.add(
            viewModel.loadDownloadedCredentials(contactPack).subscribe(
                { credList ->
                    section.setDataList(credList)
                    adapter.addSection(section)
                    adapter.notifyDataSetChanged()
                },
                {
                    it.printStackTrace()
                })
        )
        clickDisposable.add(
            section.listenToClickEvents().subscribe({
                goToCredentialDetails(it)
            }, rxError("Failed to listen to click"))
        )
        clickDisposable.add(
            section.listenToFooterClickEvents().subscribe({
                downloadCredentials()
            }, rxError("Failed to listen to clicks"))
        )
    }

    private fun loadUploadedCredsRecycler() {
        binding.cdUploadedPassesRv.layoutManager = LinearLayoutManager(requireContext())
        val section = UDCredentialSection(
            R.string.cd_upload_creds,
            R.drawable.ic_add_plus,
            viewModel.shouldDisableFeatureForRegion()
        )
        val adapter = SectionedRecyclerViewAdapter()
        binding.cdUploadedPassesRv.adapter = adapter

        viewModel.loadUploadedCredentials(contactPack).subscribe(
            object : SingleObserver<ArrayList<Package>> {
                override fun onSubscribe(d: Disposable?) {
                    loadingDisposable.add(d)
                }

                override fun onSuccess(credList: ArrayList<Package>) {
                    section.setDataList(credList)
                    adapter.addSection(section)
                    adapter.notifyDataSetChanged()
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }
            }
        )
        clickDisposable.add(
            section.listenToClickEvents().subscribe({
                goToCredentialDetails(it)
            }, rxError("Failed to listen to click"))
        )
        clickDisposable.add(
            section.listenToFooterClickEvents().subscribe({
                findNavController().navigate(
                    R.id.global_action_credentials_for_upload, bundleOf(
                        ConsentFragment.KEY_CONTACT to contactPack
                    )
                )
            }, rxError("Failed to listen to click"))
        )
    }

    private fun goToCredentialDetails(cp: Package) {
        findNavController().navigate(
            R.id.global_action_credential_details, bundleOf(
                CredentialDetailsFragment.KEY_CREDENTIAL_PACKAGE to cp
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        RxHelper.unsubscribe(deleteDisposable.get(), loadingDisposable, clickDisposable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun downloadCredentials() {
        showLoading(true)
        loadingDisposable.add(
            viewModel.downloadCredentials(contactPack)
                .doFinally { showLoading(false) }
                .subscribe({
                    if (it.isSuccessfulAndHasBody()) {
                        val aPackages = it.body()!!
                            .payload.toPackage(contactPack.getSymmetricKey())
                            .toArrayList()
                        if (aPackages.isNotEmpty()) {
                            findNavController().navigate(
                                R.id.global_action_to_scan_verify,
                                bundleOf(
                                    ScanVerifyFragment.KEY_IS_CONTACT to false,
                                    ScanVerifyFragment.KEY_CREDENTIAL_PACKAGE_LIST to aPackages,
                                    ScanVerifyFragment.KEY_ORGANIZATION_NAME to contactPack.getOrgId(),
                                )
                            )
                        } else {
                            view?.snackShort(getString(R.string.no_credentials_to_download))
                        }
                    } else {
                        handleCommonErrors(it)
                    }
                }, errorConsumer())
        )
    }

    companion object {
        const val KEY_CONTACT_PACKAGE = "contactPackage"
        const val KEY_DOWNLOAD = "download_credentials"

        private fun JsonObject.getFormattedAddress(): CharSequence {
            val formattedAddress = StringBuilder()

            if (this.isJsonNull) {
                return formattedAddress
            }

            if (this.has("line")) {
                formattedAddress.append(getStringOrNull("line").orEmpty())
                    .appendLine()
            }
            if (this.has("city")) {
                formattedAddress.append(getStringOrNull("city").orEmpty())
                    .append(", ")
            }
            if (this.has("state")) {
                formattedAddress.append(getStringOrNull("state").orEmpty())
                    .append(' ')
            }
            if (this.has("postalCode")) {
                formattedAddress.append(getStringOrNull("postalCode").orEmpty())
                    .appendLine()
            }
            if (this.has("country")) {
                formattedAddress.append(getStringOrNull("country"))
            }
            return formattedAddress
        }
    }
}