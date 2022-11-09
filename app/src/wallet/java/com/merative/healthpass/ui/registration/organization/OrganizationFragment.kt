package com.merative.healthpass.ui.registration.organization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragOrganizationBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.api.registration.hit.RegistrationResponse
import com.merative.healthpass.models.api.registration.nih.DisplaySchemaResponse
import com.merative.healthpass.ui.registration.BaseRegistrationFragment
import com.merative.healthpass.ui.registration.registerCode.RegistrationCodeFragment
import com.merative.healthpass.utils.RxHelper
import io.reactivex.rxjava3.disposables.SerialDisposable
import retrofit2.Response

class OrganizationFragment : BaseRegistrationFragment() {
    private var _binding: FragOrganizationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var viewModel: OrgViewModel
    private val organizationName by lazy {
        arguments?.getString(KEY_ORGANIZATION_NAME)
    }

    private val code: String? by lazy {
        arguments?.getString(KEY_REGISTRATION_CODE)
    }

    private val disposable = SerialDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragOrganizationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(OrgViewModel::class.java)

        binding.btnNext.setOnClickListener {
            requestInfo()
        }

        binding.til.editText?.apply {
            doAfterTextChanged { editable ->
                binding.btnNext.isEnabled = editable.isNotNullOrEmpty()
            }
            setText(organizationName)

            onDone { requestInfo() }

            if (organizationName.isNotNullOrEmpty()) {
                binding.btnNext.performClick()
            }
        }
    }

    private fun requestInfo() {
        view?.hideKeyboard()
        val org = binding.til.editText?.text?.toString()?.trim().orEmpty()
        if (org.isNotNullOrEmpty()) {
            when {
                isNIHFlow(org) -> requestNih()
                else -> requestHit()
            }
        }
    }

    //region hit & ncl
    private fun requestHit() {
        showLoading(true)
        disposable.set(
            viewModel.requestOrgInfo(binding.til.editText?.text.toString().trim())
                .doFinally { showLoading(false) }
                .subscribe({
                    if (it.isSuccessfulAndHasBody()) {
                        handleHitSuccess(it)
                    } else {
                        handleCommonErrors(it)
                    }
                }, errorConsumer())
        )
    }

    private fun handleHitSuccess(response: Response<RegistrationResponse>) {
        val bundle = bundleOf(
            KEY_REGISTRATION_CODE to code,
            KEY_ORGANIZATION_NAME to binding.til.editText?.text.toString().trim(),
            RegistrationCodeFragment.KEY_HIT_RESPONSE to response.body()!!.payload
        )
        if (response.body()?.payload != null) {
            when {
                response.body()?.payload?.flow?.registrationCodeAuth == true -> {
                    findNavController().navigate(
                        R.id.action_organizationFragment_to_registrationFragment,
                        bundle
                    )
                }
                response.body()?.payload?.flow?.showUserAgreement == true -> {
                    findNavController().navigate(
                        R.id.action_organizationFragment_to_userAgreementFragment,
                        bundle
                    )
                }
                response.body()?.payload?.flow?.showRegistrationForm == true -> {
                    findNavController().navigate(
                        R.id.action_organizationFragment_to_registrationDetailsFragment,
                        bundle
                    )
                }
                else -> {
                    loge("missing handled case for registration HIT response")
                }
            }
        } else {
            loge("empty handled case for registration HIT response")
        }
    }
    //endregion

    //region nih
    private fun requestNih() {
        showLoading(true)
        disposable.set(
            viewModel.requestNih(binding.til.editText?.text.toString().trim())
                .doFinally { showLoading(false) }
                .subscribe({
                    if (it.isSuccessful) {
                        handleNihSuccess(it.body())
                    } else {
                        handleCommonErrors(it)
                    }
                }, errorConsumer())
        )
    }

    private fun handleNihSuccess(response: DisplaySchemaResponse?) {
        findNavController().navigate(
            R.id.action_organizationFragment_to_userAgreementFragment,
            bundleOf(
                KEY_REGISTRATION_CODE to code,
                KEY_ORGANIZATION_NAME to binding.til.editText?.text.toString().trim(),
                RegistrationCodeFragment.KEY_NIH_RESPONSE to response
            )
        )
    }
    //endregion

    override fun onDestroy() {
        super.onDestroy()
        RxHelper.unsubscribe(disposable)
        _binding = null
    }

    companion object {
        //those are used for deep linking, keep value of string
        //This key is also shared across next screens, be careful while changing them
        const val KEY_ORGANIZATION_NAME = "org"
        const val KEY_REGISTRATION_CODE = "code"
    }
}