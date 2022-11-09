package com.merative.healthpass.ui.registration.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragVerificationCodeBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.api.registration.hit.RegistrationPayLoad
import com.merative.healthpass.models.api.registration.hit.VerificationCodeResponse
import com.merative.healthpass.ui.registration.BaseRegistrationFragment
import com.merative.healthpass.ui.registration.organization.OrganizationFragment
import com.merative.healthpass.ui.registration.registerCode.RegistrationCodeFragment
import com.merative.healthpass.ui.registration.registerCode.RegistrationCodeVM
import com.merative.healthpass.utils.RxHelper
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import retrofit2.Response

class VerificationCodeFragment : BaseRegistrationFragment() {
    private var _binding: FragVerificationCodeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var viewModel: VerificationViewModel
    lateinit var registerViewModel: RegistrationCodeVM

    private val requestDisposables = CompositeDisposable()
    private val smsDisposable = SerialDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerSmsReceiver()
        listenToSms()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragVerificationCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(VerificationViewModel::class.java)
        registerViewModel =
            createViewModel(RegistrationCodeVM::class.java)

        binding.til.editText?.apply {
            doAfterTextChanged { editable ->
                binding.btnNext.isEnabled = editable.isNotNullOrEmpty()
            }

            onDone {
                if (text.isNotNullOrEmpty()) {
                    verifyCode(text.toString())
                }
            }
        }

        binding.til.editText?.setText(arguments?.getString(KEY_VERIFICATION_CODE))

        binding.btnNext.setOnClickListener {
            verifyCode(binding.til.editText?.text.toString())
        }

        binding.bwtResend.setOnClickListener {
            requestVerificationCode()
        }
    }

    //region verification code
    private fun verifyCode(code: String) {
        val registrationPayLoad =
            arguments?.get(RegistrationCodeFragment.KEY_HIT_RESPONSE) as? RegistrationPayLoad
        showLoading(true)
        requestDisposables.add(
            viewModel.requestInfo(registrationPayLoad?.org.orEmpty(), code)
                .doFinally { showLoading(false) }
                .subscribe({
                    if (it.isSuccessfulAndHasBody()) {
                        handleVerificationSuccess(it)
                    } else {
                        handleCommonErrors(it)
                    }
                }, rxError("failed to request info"))
        )
    }

    private fun handleVerificationSuccess(response: Response<VerificationCodeResponse>) {
        val registrationPayLoad =
            arguments?.get(RegistrationCodeFragment.KEY_HIT_RESPONSE) as? RegistrationPayLoad

        if (registrationPayLoad?.flow?.showUserAgreement == true) {
            findNavController().navigate(
                R.id.action_verificationFragment_to_userAgreementFragment,
                bundleOf(
                    OrganizationFragment.KEY_ORGANIZATION_NAME to arguments?.getString(
                        OrganizationFragment.KEY_ORGANIZATION_NAME
                    ),
                    OrganizationFragment.KEY_REGISTRATION_CODE to arguments?.getString(
                        OrganizationFragment.KEY_REGISTRATION_CODE
                    ),
                    RegistrationCodeFragment.KEY_HIT_RESPONSE to arguments?.get(
                        RegistrationCodeFragment.KEY_HIT_RESPONSE
                    ) as? RegistrationPayLoad,
                )
            )
        }
    }
    //endregion

    //region request verification code again
    private fun requestVerificationCode() {
        val code = arguments?.getString(OrganizationFragment.KEY_REGISTRATION_CODE).orEmpty()
        val registrationPayLoad =
            arguments?.get(RegistrationCodeFragment.KEY_HIT_RESPONSE) as? RegistrationPayLoad

        showLoading(true)

        //the receiver works for one time only, so register it again to make sure to read again for the user
        unregisterSmsReceiver()
        registerSmsReceiver()

        requestDisposables.add(
            registerViewModel.requestHit(registrationPayLoad?.org.orEmpty(), code)
                .doFinally { showLoading(false) }
                .subscribe({
                    if (it.isSuccessfulAndHasBody()) {
                        //receiver should read the value
                    } else {
                        binding.bwtResend.reset()
                        handleCommonErrors(it)
                    }
                }, rxError("failed to request hit/verification"))
        )
    }
    //endregion

    //region sms
    private fun listenToSms() {
        smsDisposable.set(
            smsEvents
                .subscribe({
                    binding.til.editText?.setText(it)
                    verifyCode(it)
                }, rxError("failed to listen to code events"))
        )

    }
    //endregion

    override fun onDestroyView() {
        RxHelper.unsubscribe(smsDisposable.get())
        unregisterSmsReceiver()
        RxHelper.unsubscribe(smsDisposable.get(), requestDisposables)
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_VERIFICATION_CODE = "verification_code"
    }
}