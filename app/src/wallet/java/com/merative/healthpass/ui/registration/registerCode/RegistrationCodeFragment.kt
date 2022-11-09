package com.merative.healthpass.ui.registration.registerCode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragRegistrationCodeBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.api.BaseResponse
import com.merative.healthpass.models.api.registration.hit.RegistrationPayLoad
import com.merative.healthpass.models.api.registration.nih.DisplaySchemaResponse
import com.merative.healthpass.ui.registration.BaseRegistrationFragment
import com.merative.healthpass.ui.registration.organization.OrganizationFragment
import com.merative.healthpass.utils.RxHelper
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import retrofit2.Response

class RegistrationCodeFragment : BaseRegistrationFragment() {
    private var _binding: FragRegistrationCodeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var viewModel: RegistrationCodeVM
    private val smsDisposable = SerialDisposable()
    private val requestDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragRegistrationCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(RegistrationCodeVM::class.java)

        val regCode = arguments?.getString(OrganizationFragment.KEY_REGISTRATION_CODE)

        binding.btnNext.setOnClickListener {
            requestVerificationCode()
        }

        binding.til.editText?.apply {
            doAfterTextChanged { editable ->
                binding.btnNext.isEnabled = editable.isNotNullOrEmpty()
            }

            if (arguments?.getString(OrganizationFragment.KEY_ORGANIZATION_NAME)
                    .equals(KEY_MERATIVE_RTO_ORG)
            ) {
                setText(KEY_MERATIVE_RTO_CODE)
            } else {
                setText(regCode)
            }

            onDone {
                if (text.isNotNullOrEmpty()) {
                    requestVerificationCode()
                }
            }

            if (regCode.isNotNullOrEmpty()) {
                binding.btnNext.performClick()
            }
        }
    }

    private fun requestVerificationCode() {
        view?.hideKeyboard()

        val org = arguments?.getString(OrganizationFragment.KEY_ORGANIZATION_NAME)?.trim().orEmpty()
        val flow = (arguments?.get(KEY_HIT_RESPONSE) as? RegistrationPayLoad)?.flow

        when {
            isNIHFlow(org) -> requestNih()
            flow?.mfaAuth == false -> requestNcl()
            flow?.mfaAuth == true -> requestHit()
            else -> {
                //this is fall back for now, but it should not get triggered
                loge("the flow for $org is unknown, debug and adjust the code")
            }
        }
    }

    //region hit
    private fun requestHit() {
        showLoading(true)
        val code = binding.til.editText?.text.toString()
        val registrationPayLoad = arguments?.get(KEY_HIT_RESPONSE) as? RegistrationPayLoad

        requestDisposable.add(viewModel.requestHit(registrationPayLoad?.org.orEmpty(), code)
            .doFinally { showLoading(false) }
            .subscribe({
                if (it.isSuccessfulAndHasBody()) {
                    handleHitSuccess(it)
                } else {
                    handleCommonErrors(it)
                }
            }, rxError("failed to request hit"))
        )
    }

    private fun handleHitSuccess(response: Response<BaseResponse>) {
        val registrationPayLoad = arguments?.get(KEY_HIT_RESPONSE) as? RegistrationPayLoad
        val bundle = bundleOf(
            OrganizationFragment.KEY_ORGANIZATION_NAME to arguments?.getString(
                OrganizationFragment.KEY_ORGANIZATION_NAME
            ),
            OrganizationFragment.KEY_REGISTRATION_CODE to binding.til.editText?.text?.toString()
                .orEmpty(),
            KEY_HIT_RESPONSE to arguments?.get(KEY_HIT_RESPONSE) as? RegistrationPayLoad
        )

        if (registrationPayLoad?.flow?.mfaAuth == true) {
            findNavController().navigate(
                R.id.action_registrationFragment_to_verificationFragment,
                bundle
            )
        } else if (registrationPayLoad?.flow?.showUserAgreement == true) {
            findNavController().navigate(
                R.id.action_registrationFragment_to_userAgreementFragment,
                bundle
            )
        } else if (registrationPayLoad?.flow?.showRegistrationForm == true) {
            findNavController().navigate(
                R.id.action_registrationCodeFragment_to_registrationDetailsFragment,
                bundle
            )
        }
    }
    //endregion

    //region nih
    private fun requestNih() {
        showLoading(true)
        val code = binding.til.editText?.text.toString().trim()
        val response = arguments?.get(KEY_NIH_RESPONSE) as? DisplaySchemaResponse
        val orgName = arguments?.getString(OrganizationFragment.KEY_ORGANIZATION_NAME).orEmpty()

        requestDisposable.add(
            viewModel.validateNihCode(orgName, code)
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

    private fun handleNihSuccess(result: BaseResponse?) {
        val code = arguments?.getString(OrganizationFragment.KEY_REGISTRATION_CODE)
        if (code.isNullOrEmpty()) {
            //save the code if it was not sent in the bundle
            arguments?.putString(
                OrganizationFragment.KEY_REGISTRATION_CODE,
                binding.til.editText?.text.toString()
            )
        }
        findNavController().navigate(
            R.id.action_registrationCodeFragment_to_registrationDetailsFragment,
            arguments
        )
    }
    //endregion

    //region ncl
    private fun requestNcl() {
        showLoading(true)
        val code = binding.til.editText?.text.toString().trim()
        val orgName = arguments?.getString(OrganizationFragment.KEY_ORGANIZATION_NAME).orEmpty()

        requestDisposable.add(
            viewModel.validateNclCode(orgName, code)
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
    //endregion ncl

    override fun onResume() {
        super.onResume()
        lockAppBar(binding.scrollView, false)
    }

    override fun onDestroyView() {
        RxHelper.unsubscribe(smsDisposable.get(), requestDisposable)
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_HIT_RESPONSE = "hit_response"
        const val KEY_NIH_RESPONSE = "nih_response"

        //MERATIVE RTO Keys
        const val KEY_MERATIVE_RTO_ORG = "merative-rto"
        const val KEY_MERATIVE_RTO_CODE = "MERATIVEEMPLOYEE"
    }
}