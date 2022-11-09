package com.merative.healthpass.ui.settings.kiosk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragKioskSettingsBinding
import com.merative.healthpass.models.Duration
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.settings.kiosk.DurationDialog.Companion.DURATION_FRAGMENT_TAG

class KioskSettingsFragment : BaseFragment(), DurationDialog.DurationDialogListener {

    private var _binding: FragKioskSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    protected val binding get() = _binding!!

    lateinit var viewModel: KioskSettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = createViewModel(KioskSettingsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragKioskSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val kioskEnabled = viewModel.isKioskModeEnabled()

        binding.kioskModeSwitch.isChecked = kioskEnabled
        binding.dismissDurationSwitch.isChecked = viewModel.getAutoDismiss()
        binding.cameraSwitch.isChecked = viewModel.getDefaultFrontCamera()
        binding.dismissDurationLabel.binding.right.text =
            getString(R.string.kiosk_mode_auto_dismiss_sec, viewModel.getAutoDismissDuration())

        binding.kioskModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setKioskModeEnabled(isChecked)
            changeKioskSettingsVisibility(isChecked)
        }
        binding.dismissDurationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoDismiss(isChecked)
        }
        binding.cameraSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setDefaultFrontCamera(isChecked)
        }
        binding.dismissDurationLabel.setOnClickListener { _ ->
            DurationDialog
                .newInstance(viewModel.getDurationList())
                .apply { this.setTargetFragment(this@KioskSettingsFragment, 0) }
                .show(requireFragmentManager(), DURATION_FRAGMENT_TAG)
        }
        changeKioskSettingsVisibility(kioskEnabled)
    }

    private fun changeKioskSettingsVisibility(enabled: Boolean) {
        binding.dismissCardView.isVisible = enabled
        binding.dismissDurationCardView.isVisible = enabled
        binding.autoDismissTextView.isVisible = enabled
        binding.cameraSettingsTextView.isVisible = enabled
        binding.cameraCardView.isVisible = enabled
    }

    override fun onDurationItemClick(input: Duration) {
        viewModel.setAutoDismissDuration(input.interval)
        binding.dismissDurationLabel.binding.right.text =
            getString(R.string.kiosk_mode_auto_dismiss_sec, input.interval)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
