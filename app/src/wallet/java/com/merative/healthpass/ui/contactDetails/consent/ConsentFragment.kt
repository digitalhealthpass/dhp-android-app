package com.merative.healthpass.ui.contactDetails.consent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragConsentBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.api.ApiDataException
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import com.merative.healthpass.ui.contactDetails.uploadComplete.UploadCompleteFragment
import com.merative.healthpass.utils.RxHelper
import com.merative.watson.healthpass.verifiablecredential.extensions.getStringOrNull
import io.reactivex.rxjava3.disposables.SerialDisposable

class ConsentFragment : BaseFragment() {

    private var _binding: FragConsentBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: ConsentVM
    private val submitDisposable = SerialDisposable()
    private val consentDisposable = SerialDisposable()

    val contactPackage: ContactPackage by lazy { arguments?.get(KEY_CONTACT) as ContactPackage }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragConsentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.consentFragmentParent.visibility = View.INVISIBLE
        viewModel = createViewModel(ConsentVM::class.java)

        showLoading(true)
        consentDisposable.set(
            viewModel.requestConsentReceipt(contactPackage)
                .doFinally { showLoading(false) }
                .subscribe({
                    if (it.isSuccessfulAndHasBody()) {
                        binding.consentFragmentParent.visibility = View.VISIBLE
                        adjustView(it.body()!!.payload)
                    } else {
                        handleCommonErrors(it)
                    }
                }, errorConsumer("consent request not found"))
        )
    }

    private fun adjustView(jsonObject: JsonObject) {
        if (!contactPackage.isDefaultPackage()) {
            adjustNih(jsonObject)
        } else {
            adjustDefault(jsonObject)
        }

        binding.recyclerViewSelectedCredentials.apply {
            layoutManager = LinearLayoutManager(activity)

            adapter = CredentialsAdapter(false).apply {
                setItems((arguments?.get(KEY_CREDENTIAL_LIST) as? ArrayList<Package>).orEmpty())
            }
        }

        binding.btnAccept.setOnClickListener { submit() }
    }

    private fun adjustNih(jsonObject: JsonObject) {
        val firstService = jsonObject
            .getAsJsonArray("services")?.get(0)?.asJsonObject

        val firstPurpose = firstService?.getAsJsonArray("purposes")
            ?.get(0)?.asJsonObject

        ViewUtils.bindSimpleView(
            binding.sriService.root,
            getString(R.string.service),
            firstService?.asJsonObject?.getStringOrNull("service").orEmpty(),
            false
        )

        ViewUtils.bindSimpleView(
            binding.sriCategory.root,
            getString(R.string.category),
            firstPurpose?.asJsonObject?.getStringOrNull("purposeCategory")
                .orEmpty(),
            true
        )

        binding.categoryDescription.text =
            firstPurpose?.asJsonObject?.getStringOrNull("purpose").orEmpty()
    }

    private fun adjustDefault(jsonObject: JsonObject) {
        val firstService = jsonObject
            .getAsJsonArray("services")?.get(0)?.asJsonObject

        ViewUtils.bindSimpleView(
            binding.sriService.root,
            getString(R.string.service),
            firstService?.asJsonObject?.getStringOrNull("description").orDash(),
            false
        )

        val firstPurpose = firstService?.getAsJsonArray("purposes")
            ?.get(0)?.asJsonObject

        val piiCategoryJson = firstPurpose?.asJsonObject?.getAsJsonObject("piiCategory")
        val piiString = piiCategoryJson?.keySet()?.joinToString(" ").orDash()

        ViewUtils.bindSimpleView(
            binding.sriCategory.root,
            getString(R.string.category),
            piiString,
            true
        )

        val purposeString = firstPurpose?.asJsonObject?.getStringOrNull("description")
        ViewUtils.bindSimpleView(
            binding.sriPurpose.root,
            getString(R.string.contact_purpose),
            "",
            false
        )
        binding.categoryDescription.text = purposeString.orDash()
    }

    private fun submit() {
        showLoading(true)
        val credentialList = (arguments?.get(KEY_CREDENTIAL_LIST) as? ArrayList<Package>)
            .orEmpty()

        submitDisposable.set(
            viewModel.uploadDocument(
                arguments?.get(KEY_CONTACT) as ContactPackage,
                credentialList
            ).doFinally { showLoading(false) }
                .subscribe({
                    val responseBody = it.body()
                    if (it.isSuccessful && responseBody != null) {
                        findNavController().navigate(
                            R.id.action_consentFragment_to_uploadCompleteFragment,
                            bundleOf(
                                UploadCompleteFragment.KEY_CREDENTIALS_LIST to credentialList,
                                UploadCompleteFragment.KEY_SUBMIT_RESPONSE to responseBody.payload
                            )
                        )
                    } else {
                        handleCommonErrors(it)
                    }
                }, {
                    if (it is ApiDataException) {
                        loge(getString(R.string.api_missing_request_info), it)
                        activity?.showDialog(
                            null,
                            getString(R.string.api_missing_request_info),
                            getString(R.string.button_title_ok)
                        ) {}
                    } else {
                        handleCommonErrors(it)
                    }
                })
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        RxHelper.unsubscribe(submitDisposable.get(), consentDisposable.get())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_CONTACT = "contact"
        const val KEY_CREDENTIAL_LIST = "credential_package_list"
    }
}