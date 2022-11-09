package com.merative.healthpass.ui.landing

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.merative.healthpass.R
import com.merative.healthpass.ui.privacy.PrivacyPolicyFragment
import com.merative.watson.healthpass.scan.ScanFragment

class LandingFragment : BaseLandingFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configKiosk()
    }

    private fun configKiosk() {
        viewModel.setKioskModeEnabled(resources.getBoolean(R.bool.kiosk_mode_enabled))
        viewModel.setAutoDismiss(resources.getBoolean(R.bool.kiosk_mode_always_auto_dismiss))
        viewModel.setDefaultFrontCamera(resources.getBoolean(R.bool.kiosk_mode_front_camera))
        viewModel.setAutoDismissDuration()
    }

    override fun determineDestination() {
        super.determineDestination()

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
        } else if (!hasCameraPermission()) {
            findNavController().navigate(R.id.global_action_to_scanner_permissions_frag)
        } else {
            findNavController().navigate(
                R.id.global_action_pop_to_home, bundleOf(
                    ScanFragment.KEY_SHOW_TORCH to true,
                    ScanFragment.KEY_TOGGLE_CAMERA to true
                )
            )
        }
    }

    override fun hasDeepLink(): Boolean = false

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
}