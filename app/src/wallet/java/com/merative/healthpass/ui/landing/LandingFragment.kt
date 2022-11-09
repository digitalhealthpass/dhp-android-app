package com.merative.healthpass.ui.landing

import android.net.Uri
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.merative.healthpass.R
import com.merative.healthpass.extensions.isNotNullOrEmpty
import com.merative.healthpass.ui.home.HomeFragment
import com.merative.healthpass.ui.mainActivity.MainActivity
import com.merative.healthpass.ui.pin.PinFragment
import com.merative.healthpass.ui.privacy.PrivacyPolicyFragment
import com.merative.healthpass.ui.registration.organization.OrganizationFragment
import com.merative.healthpass.ui.scanVerify.ScanVerifyFragment


class LandingFragment : BaseLandingFragment() {

    override fun determineDestination() {
        super.determineDestination()
        if (!hasDeepLink() || viewModel.shouldDisableFeatureForRegion()) goToNextScreen()
        else {
            (activity as MainActivity?)?.handledDeepLink()
        }
    }

    private fun goToNextScreen() {
        if (!viewModel.isTermsAccepted()) {
            findNavController().navigate(R.id.global_action_to_terms_frag)
        } else if (!viewModel.isPrivacyAccepted()) {
            //adding flag to differentiate between virgin flow and hyperlink click
            findNavController().navigate(
                R.id.global_action_to_privacy_frag,
                bundleOf(
                    PrivacyPolicyFragment.KEY_IS_INITIAL_FLOW to true
                )
            )
        } else if (!viewModel.isEnvRegionSet()) {
            findNavController().navigate(R.id.global_action_to_region_selection_frag)
        } else if (viewModel.canShowTutorial()) {
            findNavController().navigate(R.id.global_action_to_tutorial_frag)
        } else if (mainActivityVM.isPinValidationRequired) {
            val flow = if (viewModel.hasPin()) {
                PinFragment.FLOW_AUTHENTICATE
            } else {
                PinFragment.FLOW_CREATE
            }
            findNavController().navigate(
                R.id.global_action_to_pin_frag,
                bundleOf(PinFragment.KEY_FLOW to flow)
            )
        } else {
            goToWallet()
        }
    }

    override fun hasDeepLink(): Boolean {
        val intent = requireActivity().intent
        val data: Uri? = intent.data
        val orgCode = data?.getQueryParameter(OrganizationFragment.KEY_ORGANIZATION_NAME)

        return arguments?.getString(HomeFragment.KEY_CONTACT_CRED_ID)
            .isNotNullOrEmpty()
                || arguments?.getString(OrganizationFragment.KEY_ORGANIZATION_NAME)
            .isNotNullOrEmpty()
                || arguments?.getString(ScanVerifyFragment.KEY_QR_CREDENTIAL_JSON_ENCODED)
            .isNotNullOrEmpty()
                || orgCode.isNotNullOrEmpty()
                || (activity as MainActivity).isSharedFile(activity?.intent)

    }
}