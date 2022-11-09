package com.merative.healthpass.ui.results

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragmentScanResultsBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.OfflineModeException
import com.merative.healthpass.models.results.ResultsModel
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.models.specificationconfiguration.SpecificationConfiguration
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.credential.details.DetailsAdapter
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.healthpass.utils.schema.Field
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import com.merative.watson.healthpass.verificationengine.exception.VerifyError
import com.merative.watson.healthpass.verificationengine.known.UnKnownTypeException
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo

class ScanResultsFragment : BaseFragment() {

    private var _binding: FragmentScanResultsBinding? = null
    private val binding get() = _binding!!
    private val backPressCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
        }
    }

    private lateinit var viewModel: ScanResultsViewModel

    private val sectionAdapter = SectionedRecyclerViewAdapter()
    private lateinit var specSection: CredSpecSection
    private lateinit var typeSection: CredTypeSection
    private lateinit var issuerSection: IssuerSection

    private val disposables = CompositeDisposable()

    private var menuInfo: MenuItem? = null

    private lateinit var aPackage: Package

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = createViewModel(ScanResultsViewModel::class.java)
        viewModel.init(
            flavorVM.selectedCredential!!,
            requireArguments().getString(CREDENTIAL_JSON).orValue("{}"),
        )

        aPackage = Package(
            VerifiableObject(arguments?.getString(CREDENTIAL_JSON).toString()),
            null, null
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = FragmentScanResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            backPressCallback
        )

        verify()
    }

    private fun adjustSpecSection() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity())

            specSection = CredSpecSection(getString(R.string.result_credentialSpec))
            sectionAdapter.addIfNotExist(specSection)
            val spec = viewModel.getType()
            specSection.setDataList(listOf(spec))

            adapter = sectionAdapter
        }
    }

    private fun addCredType(specificationConfiguration: SpecificationConfiguration) {
        if (specificationConfiguration.credentialCategoryDisplayValue.isNotNullOrEmpty()
            || specificationConfiguration.description.isNotNullOrEmpty()
        ) {
            typeSection = CredTypeSection(getString(R.string.result_credentialType))
            sectionAdapter.addSection(typeSection)
            typeSection.setDataList(listOf(specificationConfiguration))
            sectionAdapter.notifyDataSetChanged()
        }
    }

    private fun addIssuerSection(issuerName: String?) {
        issuerSection = IssuerSection("Issuer")
        sectionAdapter.addIfNotExist(issuerSection)
        issuerSection.setDataList(
            listOf(issuerName)
        )
        sectionAdapter.notifyDataSetChanged()
    }

    private fun adjustCard(isValid: Boolean, throwable: Throwable?) {
        binding.dismissButton.isVisible = true
        when {
            isValid -> {
                showReason()
                binding.scanResultTopCard.setCardBackgroundColor(
                    requireContext().getColorCompat(R.color.switch_on)
                )
                binding.credentialsResultIcon.setImageResource(R.drawable.ic_valid_credentials)
                binding.resultTitle.text = getText(R.string.result_Verified)

                if (viewModel.isKioskModeEnabled()) {
                    setCountdownDismissButton()
                    startCountdown()
                } else {
                    setDefaultDismissButton()
                }

                if (viewModel.isHapticEnabled()) {
                    activity?.let { verifiedShake(it) }
                }

                if (viewModel.isSoundEnabled()) {
                    //Play success sound
                    playRing(true)
                }
            }
            else -> {
                showReason(throwable)
                binding.scanResultTopCard.setCardBackgroundColor(
                    requireContext().getColorCompat(R.color.red)
                )
                binding.credentialsResultIcon.setImageResource(R.drawable.ic_cancel_blue_24dp)
                binding.resultTitle.text = getText(R.string.result_notVerified)

                if (viewModel.isKioskModeEnabled() && viewModel.isAutoDismiss()) {
                    setCountdownDismissButton()
                    startCountdown()
                } else {
                    setDefaultDismissButton()
                }

                if (viewModel.isHapticEnabled()) {
                    activity?.let { unverifiedShake(it) }
                }

                if (viewModel.isSoundEnabled()) {
                    //Play failure sound
                    playRing(false)
                }
            }
        }
    }

    private fun setCountdownDismissButton() {
        val duration = viewModel.getAutoDismissDuration()
        binding.dismissButton.text = getString(R.string.result_CloseInSec, duration)
        binding.dismissButton.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.red)
        binding.dismissButton.setOnClickListener {
            RxHelper.unsubscribe(disposables)
            setDefaultDismissButton()
        }
    }

    private fun setDefaultDismissButton() {
        binding.dismissButton.text = getString(R.string.result_scanNext)
        binding.dismissButton.icon = context?.getDrawable(R.drawable.ic_qr_code_scanner_black_24dp)
        binding.dismissButton.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
        binding.dismissButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun showReason(throwable: Throwable? = null) {

        menuInfo?.setOnMenuItemClickListener {
            val title = binding.resultTitle.text as String
            var message = throwable?.localizedMessage.orValue("Unknown Error")
            when {
                (title == getText(R.string.result_Verified)) -> {
                    message = getText(R.string.verification_successful) as String
                }
                else -> {
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
                        (throwable is NullPointerException) -> {
                            message =
                                getText(R.string.result_credential_unknown) as String
                        }
                    }
                }
            }

            activity?.showTermsDialog(
                title, message, "", {}, getString(R.string.button_title_ok), {}
            )
            true
        }
    }

    private fun verify() {
        showLoading(true)
        enableCallBack(true)
        viewModel.validate(checkNetworkState())
            .doFinally {
                showLoading(false)
                enableCallBack(false)
            }
            .asyncToUiSingle()
            .subscribe({
                adjustCard(it.status, it.throwable)
                adjustSpecSection()
                showDetails(it)
            }, {
                if (it is OfflineModeException) {
                    showOfflineModeError()
                    adjustCard(false, it)
                } else {
                    adjustCard(false, it)
                }
                adjustSpecSection()
                it.printStackTrace()
            })
            .addTo(disposables)
    }

    private fun startCountdown() {
        viewModel.startCountdown()
            .doOnComplete {
                RxHelper.unsubscribe(disposables)
                view?.post { findNavController().popBackStack() }
            }
            .doOnNext {
                binding.dismissButton.text = getString(R.string.result_CloseInSec, it)
            }
            .subscribe()
            .addTo(disposables)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_scan_results, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menuInfo = menu.findItem(R.id.action_info)
    }

    private fun showDetails(resultsModel: ResultsModel) {
      when (resultsModel.throwable) {
            is UnKnownTypeException -> loge(
                "UnknownVCredentialsError",
                resultsModel.throwable
            )
        }
        resultsModel.specificationConfiguration?.let { addCredType(it) }
        if (resultsModel.status) {
            addIssuerSection(resultsModel.issuerName)
        }
        if (resultsModel.displayList.isNotEmpty()) {
            val fieldList: MutableList<Field> = ArrayList()
            resultsModel.displayList.forEach {
                val field = Field(it.first, it.second, null)
                fieldList.add(field)
            }
            setDetailsSection(fieldList)
        }
    }

    private fun setDetailsSection(fieldList: MutableList<Field>) {
        fieldList.forEach { field ->
            var header = getString(R.string.result_credentialDetails)
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
                    parsedFieldValue.reduce { it, str -> "$it : $str" }
                }
                else -> parsedFieldValue
            }

            if (updatedFieldValue != null) {
                updatedFieldValue.also { field.value = it }
                field.isObfuscated = isObfuscated(field.value)
            }

            if (loadedSection == null) {
                section.addItem(field)
                sectionAdapter.addSection(header, section)
            } else {
                (loadedSection as DetailsAdapter).addItem(field)
            }
        }
        binding.recyclerView.adapter = sectionAdapter
        sectionAdapter.notifyDataSetChanged()
    }

    private fun showOfflineModeError() {
        // ignore
    }

    private fun enableCallBack(isCallbackEnabled: Boolean) {
        backPressCallback.isEnabled = isCallbackEnabled
    }

    private fun checkNetworkState(): Boolean {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting ?: false
    }

    override fun onDestroyView() {
        RxHelper.unsubscribe(disposables)
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val CREDENTIAL_JSON = "CREDENTIAL_JSON"
    }

    private fun verifiedShake(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(100)
        }
    }

    private fun unverifiedShake(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(VibrationEffect.createOneShot(250, VibrationEffect.EFFECT_DOUBLE_CLICK))
        } else {
            (context.getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(250)
        }
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

    private fun isObfuscated(value: Any?): Boolean {
        if (value == null) return false
        
        if (value is String) {
            val regex: Regex = "^[a-zA-Z0-9_-]{42,43}\$".toRegex()
            return (value.matches(regex))
        }
        return false
    }
}