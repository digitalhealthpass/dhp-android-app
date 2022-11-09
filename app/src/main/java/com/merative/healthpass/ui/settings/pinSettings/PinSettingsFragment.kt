package com.merative.healthpass.ui.settings.pinSettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.navigation.fragment.findNavController
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragmentPinSettingsBinding
import com.merative.healthpass.extensions.showDialog
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import com.merative.healthpass.ui.pin.PinFragment
import com.merative.healthpass.utils.RxHelper
import io.reactivex.rxjava3.disposables.SerialDisposable

class PinSettingsFragment : BaseFragment() {
    private var _binding: FragmentPinSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val deletingDisposable = SerialDisposable()

    lateinit var viewModel: PinSettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentPinSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(PinSettingsViewModel::class.java)
        initUI()
    }

    override fun onResume() {
        super.onResume()
        updateViewState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        RxHelper.unsubscribe(deletingDisposable.get())
        _binding = null
    }

    private fun initUI() {
        binding.buttonCreatePin.setOnClickListener {
            val flowId = PinFragment.FLOW_SETTINGS
            findNavController().navigate(
                R.id.pinFragment,
                bundleOf(
                    PinFragment.KEY_FLOW to flowId
                )
            )
        }

        binding.buttonDeletePin.setOnClickListener {
            showDeletePinDialog()
        }

        setupSupportedLogin()
    }

    private fun setupSupportedLogin() {
        val biometricManager = BiometricManager.from(requireContext())

        val pinRightText: String =
            if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
                || viewModel.getPin().isBiometric == true
            ) {
                getString(R.string.biometric_title)
            } else {
                getString(R.string.profile_pin)
            }

        ViewUtils.bindSimpleView(
            binding.pingSettingsBioContainer.root,
            getString(R.string.supported_login_title),
            pinRightText,
            false
        )
    }

    private fun updateViewState() {
        binding.supportedLoginContainer.isGone = !viewModel.hasPin()
        binding.biometricOptionHeaderTextview.isGone = !viewModel.hasPin()
        binding.buttonDeletePin.isGone = !viewModel.hasPin()
        binding.buttonCreatePin.text =
            if (viewModel.hasPin()) getString(R.string.profile_pin_change) else getString(R.string.profile_pin_create)
    }

    private fun showDeletePinDialog() {
        activity?.showDialog(
            getString(R.string.profile_pin_deleteMessage),
            "",
            getString(R.string.profile_pin_delete), {
                deletePin()
            },
            getString(R.string.button_title_cancel), {}
        )
    }

    private fun showDeletePinFailedDialog() {
        activity?.showDialog(
            getString(R.string.pin_delete_failed_title),
            "",
            getString(R.string.button_title_ok), {},
            getString(R.string.button_title_cancel), {}
        )
    }

    private fun deletePin() {
        deletingDisposable.set(
            viewModel.deletePin()
                .subscribe({
                    updateViewState()
                }, { showDeletePinFailedDialog() })
        )
    }
}