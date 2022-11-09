package com.merative.healthpass.ui.registration.userAgreement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragAgreementBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.api.registration.hit.RegSubmitResponse
import com.merative.healthpass.models.api.registration.hit.RegistrationPayLoad
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.registration.BaseRegistrationFragment
import com.merative.healthpass.ui.registration.organization.OrganizationFragment
import com.merative.healthpass.ui.registration.registerCode.RegistrationCodeFragment
import com.merative.healthpass.ui.scanVerify.ScanVerifyFragment
import com.merative.healthpass.utils.RxHelper
import com.merative.watson.healthpass.verifiablecredential.models.credential.toVerifiableObject
import io.reactivex.rxjava3.disposables.SerialDisposable

class UserAgreementFragment : BaseRegistrationFragment() {
    private var _binding: FragAgreementBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var viewModel: UserAgreementVM
    private val requestDisposable = SerialDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragAgreementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(UserAgreementVM::class.java)

        handleAgreement()

        binding.bottomDecline.setOnClickListener {
            showBackWarning()
        }

        binding.bottomAccept.setOnClickListener {
            submit()
        }
    }

    private fun handleAgreement() {
        val org = arguments?.getString(OrganizationFragment.KEY_ORGANIZATION_NAME)?.trim().orEmpty()
        val agreementString =
            when {
                isNIHFlow(org) -> {
                    getString(R.string.nih_agreement).fromHtml()
                }
                else -> {
                    val registrationPayLoad =
                        arguments?.get(RegistrationCodeFragment.KEY_HIT_RESPONSE) as? RegistrationPayLoad

                    registrationPayLoad?.userAgreement.orEmpty().fromHtml()
                }
            }

        binding.txtAgreement.text = agreementString
    }

    private fun submit() {
        when {
            arguments?.get(RegistrationCodeFragment.KEY_NIH_RESPONSE) != null -> handleNih()
            arguments?.get(RegistrationCodeFragment.KEY_HIT_RESPONSE) != null -> handleHit()
            else -> loge("hit response is null, check why, or handle missing case")
        }
    }

    override fun onResume() {
        super.onResume()
        applyAppBarLayout {
            setExpanded(true)
        }
        removeScrollFlags()
        applySupportActionBar {
            setBackgroundDrawable(activity?.getColor(android.R.color.transparent)?.toDrawable())
        }
    }

    override fun onPause() {
        super.onPause()
        applyDefaultScrollFlags()
    }

    //region HIT
    private fun handleHit() {
        val regCode = arguments?.getString(OrganizationFragment.KEY_REGISTRATION_CODE).orEmpty()
        val orgName =
            (arguments?.get(RegistrationCodeFragment.KEY_HIT_RESPONSE) as? RegistrationPayLoad)
                ?.org.orEmpty()
        val regPayLoad =
            (arguments?.get(RegistrationCodeFragment.KEY_HIT_RESPONSE) as? RegistrationPayLoad)

        if (regPayLoad?.flow?.mfaAuth == false) {
            if (regPayLoad.flow.showRegistrationForm) {
                handleNcl()
            } else {
                val org = arguments?.getString(OrganizationFragment.KEY_ORGANIZATION_NAME)?.trim()
                    .orEmpty()
                val code = arguments?.getString(OrganizationFragment.KEY_REGISTRATION_CODE)?.trim()
                    .orEmpty()
                viewModel.submit(org, code).subscribe(
                    { response ->
                        val responseBody = response.body()
                        if (response.isSuccessful && responseBody != null) {
                            val bundle = bundleOf(
                                ScanVerifyFragment.KEY_CREDENTIAL_PACKAGE_LIST to responseBody.payload.map {
                                    Package(it.toVerifiableObject(), null, null)
                                },
                                ScanVerifyFragment.KEY_IS_CONTACT to true,
                                ScanVerifyFragment.KEY_ORGANIZATION_NAME to arguments?.getString(
                                    OrganizationFragment.KEY_ORGANIZATION_NAME
                                ),
                                ScanVerifyFragment.KEY_ASYMMETRIC_KEY to viewModel.asymmetricKey,
                            )

                            findNavController().navigate(R.id.global_action_to_scan_verify, bundle)
                        } else {
                            handleCommonErrors(response)
                        }
                    }, errorConsumer("failed to submit the nih flow")
                )
            }
        } else if (regPayLoad?.flow?.mfaAuth == true) {
            showLoading(true)
            requestDisposable.set(
                viewModel.submitRegistration(orgName, regCode)
                    .doFinally { showLoading(false) }
                    .subscribe({
                        if (it.isSuccessful && it.body() != null) {
                            handleHitSuccess(it.body()!!)
                        } else {
                            handleCommonErrors(it)
                        }
                    }, rxError("failed to submit hit info"))
            )
        }
    }

    private fun handleHitSuccess(result: RegSubmitResponse) {
        val bundle = bundleOf(
            ScanVerifyFragment.KEY_CREDENTIAL_PACKAGE_LIST to result.payload.map {
                Package(it.toVerifiableObject(), null, null)
            },
            ScanVerifyFragment.KEY_IS_CONTACT to true,
            ScanVerifyFragment.KEY_ORGANIZATION_NAME to requireArguments().getString(
                OrganizationFragment.KEY_ORGANIZATION_NAME
            ),
            ScanVerifyFragment.KEY_ASYMMETRIC_KEY to viewModel.asymmetricKey
        )

        findNavController().navigate(R.id.global_action_to_scan_verify, bundle)
    }
    //endregion

    //region NIH
    private fun handleNih() {
        findNavController().navigate(
            R.id.action_userAgreementFragment_to_registrationCodeFragment,
            arguments
        )
    }
    //endregion

    //region NCL
    private fun handleNcl() {
        findNavController().navigate(
            R.id.action_userAgreementFragment_to_registrationDetailsFragment,
            arguments
        )
    }
    //endregion

    override fun onDestroyView() {
        super.onDestroyView()
        RxHelper.unsubscribe(requestDisposable.get())
        _binding = null
    }
}