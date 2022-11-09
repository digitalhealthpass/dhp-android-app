package com.merative.healthpass.ui.pin

import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragPinBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.pin.PinCode
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.asyncToUiCompletable
import io.reactivex.rxjava3.disposables.SerialDisposable

class PinFragment : BaseFragment() {

    private var _binding: FragPinBinding? = null
    private val binding get() = _binding!!

    private val savingDisposable = SerialDisposable()
    private val deleteCredsDisposable = SerialDisposable()
    private val deletePinDisposable = SerialDisposable()
    private val skipPinDisposable = SerialDisposable()

    private val timerDuration: Long = 10000
    private val timerInterval: Long = 1000
    private lateinit var til: TextInputLayout
    private var failedAttemptCount = 0
    private var menuResetWallet: MenuItem? = null

    private lateinit var viewModel: PinViewModel

    private val isAuthenticatingFlow: Boolean by lazy {
        requireArguments().getInt(KEY_FLOW) == FLOW_AUTHENTICATE
    }
    private val isCreateFlow: Boolean by lazy {
        requireArguments().getInt(KEY_FLOW) == FLOW_CREATE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)

        _binding = FragPinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(PinViewModel::class.java)

        if (isAuthenticatingFlow
            || (isCreateFlow
                    && arguments?.getString(KEY_PIN_CODE).isNullOrEmpty())
        ) {
            biometricAuthenticate({
                validateAuthPin(null, true)
            }, {
            }, {})
        }

        if (isAuthenticatingFlow) {
            val timer = object : CountDownTimer(timerDuration, timerInterval) {
                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    menuResetWallet?.isVisible = true
                }
            }
            timer.start()
        }

        binding.til.apply {
            til = this
//            til.editText?.requestFocus()
//            til.editText?.showKeyboard()

            editText?.doAfterTextChanged { editable ->
                if (editable?.length == 4) {
                    when {
                        isAuthenticatingFlow -> {
                            validateAuthPin(editable.toString(), false)
                        }
                        arguments?.getString(KEY_PIN_CODE).isNotNullOrEmpty() -> {
                            //this is confirming the pin before saving it
                            savePin(editable.toString())
                        }
                        else -> {
                            navigateToConfirm(editable.toString())
                        }
                    }
                }
            }
        }

        binding.pinTxtHeader.apply {
            text = when {
                isAuthenticatingFlow -> {
                    getString(R.string.pin_detail_unlock)
                }
                arguments?.getString(KEY_PIN_CODE).isNullOrEmpty() -> {
                    getString(R.string.pin_detail_create)
                }
                else -> {
                    getString(R.string.pin_detail_confirm)
                }
            }
        }
        handleBack()
    }

    private fun validateAuthPin(pinString: String?, isBioMetric: Boolean) {
        val oldPinCode = viewModel.getPin()
        if (pinString == oldPinCode.code || isBioMetric) {
            if (isBioMetric) {
                val newPinCode = oldPinCode.copy(isBiometric = true)
                savingDisposable.set(
                    viewModel.savePin(newPinCode)
                        .subscribe({
                            til.editText?.hideKeyboard()
                            closePinFlow()
                        }, rxError("failed to save PIN code"))
                )
            } else {
                til.editText?.hideKeyboard()
                closePinFlow()
            }
        } else {
            performInvalidPinAction()
        }
    }

    private fun handleBack() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    handleBackClicked()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun handleBackClicked() {
        when {
            arguments?.getString(KEY_PIN_CODE).isNotNullOrEmpty() -> {
                //pin confirmation
                arguments?.remove(KEY_PIN_CODE)
                findNavController().popBackStack()
            }
            isAuthenticatingFlow -> {
                setResult(false)
                activity?.finish()
            }
            requireArguments().getInt(KEY_FLOW) == FLOW_SETTINGS -> {
                //settings, so just pop back
                findNavController().popBackStack(R.id.navigation_settings_fragment, false)
            }
            else -> {
                skipPinDisposable.set(
                    viewModel.savePin(PinCode.SKIPPED)
                        .asyncToUiCompletable()
                        .subscribe({
                            closePinFlow()
                        }, rxError("failed to skip pin"))
                )
            }
        }
    }

    private fun navigateToConfirm(pin: String) {
        findNavController().navigate(
            R.id.pinFragment,
            bundleOf(
                KEY_FLOW to arguments?.getInt(KEY_FLOW),
                KEY_PIN_CODE to til.editText?.text?.toString()
            )
        )
    }

    private fun showPinConfirmationFailedDialog() {
        activity?.showDialog(
            getString(R.string.pin_confirmation_title),
            getString(R.string.pin_confirmation_message),
            getString(R.string.pin_confirmation_skip), {
                closePinFlow()
            },
            getString(R.string.pin_confirmation_startOver), {}
        )
    }

    private fun showAuthenticateFlowInvalidPinDialog(lastAttempt: Boolean) {
        val negativeTitleString = if (lastAttempt) getString(R.string.pin_reset_title) else ""
        activity?.showDialog(
            getString(R.string.pin_invalid_title),
            getString(R.string.pin_invalid_message),
            getString(R.string.pin_retry), {},
            negativeTitleString, {
                showResetWalletDialog()
            }
        )
    }

    private fun showResetWalletDialog() {
        activity?.showDialog(
            "",
            getString(R.string.pin_reset_message),
            getString(R.string.pin_reset_title), {
                resetWallet()
            },
            getString(R.string.button_title_cancel), {}
        )
    }

    private fun showPinSavedDialog() {
        activity?.showDialog(
            getString(R.string.pin_complete_title),
            getString(R.string.pin_complete_message),
            getString(R.string.pin_complete_continue), {
                closePinFlow()
            },
            "", {}
        )
    }

    private fun resetWallet() {
        deleteCreds()
    }

    private fun showErrorDialog(title: String) {
        activity?.showDialog(
            title,
            "",
            getString(R.string.button_title_ok), {},
            getString(R.string.button_title_cancel), {}
        )
    }

    private fun showDeleteContentFailedDialog() {
        showErrorDialog(getString(R.string.content_delete_failed_title))
    }

    private fun deleteCreds() {
        deleteCredsDisposable.set(
            viewModel.resetDB()
                .subscribe({
                    closePinFlow()
                }, { showDeleteContentFailedDialog() })
        )
    }

    private fun performAuthenticateFlowInvalidPinAction() {
        failedAttemptCount++
        val lastAttempt = failedAttemptCount == 3
        showAuthenticateFlowInvalidPinDialog(lastAttempt)

        if (lastAttempt) {
            failedAttemptCount = 0
        }
    }

    private fun performInvalidPinAction() {
        til.editText?.text?.clear()
        if (isAuthenticatingFlow) {
            performAuthenticateFlowInvalidPinAction()
        } else {
            showPinConfirmationFailedDialog()
        }
    }

    private fun savePin(pinString: String) {
        if (pinString == arguments?.getString(KEY_PIN_CODE)) {
            val oldPinCode = viewModel.getPin()
            savingDisposable.set(
                viewModel.savePin(
                    oldPinCode.copy(code = pinString)
                ).subscribe({
                    showPinSavedDialog()
                }, rxError("failed to save PIN code"))
            )
        } else {
            performInvalidPinAction()
        }
    }

    //region menu
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menuResetWallet = menu.findItem(R.id.menu_reset_wallet)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_pin, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_reset_wallet -> {
                showResetWalletDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //endregion

    override fun onResume() {
        super.onResume()
        applyAppBarLayout { setExpanded(false) }
        applySupportActionBar {
            when {
                isAuthenticatingFlow -> {
                    setDisplayHomeAsUpEnabled(false)
                    title = getString(R.string.pin_title_unlock)
                }
                arguments?.getString(KEY_PIN_CODE).isNullOrEmpty() -> {
                    setHomeAsUpIndicator(R.drawable.ic_cancel_blue_24dp)
                    setHomeActionContentDescription(R.string.button_title_cancel)
                    title = getString(R.string.pin_title_create)
                }
                else -> {
                    setHomeAsUpIndicator(R.drawable.ic_arrow_back_blue_24dp)
                    title = getString(R.string.pin_title_confirm)
                }
            }
        }

        til.editText?.post {
            val focused = til.editText?.requestFocus()
            til.editText?.showKeyboard()
        }
    }

    //region closing screen
    private fun closePinFlow() {
        til.editText?.hideKeyboard()
        when (requireArguments().getInt(KEY_FLOW)) {
            FLOW_CREATE -> {
                mainActivityVM.validationFinished()
                setResult(true)
                findNavController().popBackStack(R.id.landingFragment, true)
            }
            FLOW_AUTHENTICATE -> {
                mainActivityVM.validationFinished()
                setResult(true)
                findNavController().popBackStack()
            }
            FLOW_SETTINGS -> {
                mainActivityVM.validationFinished()
                setResult(true)
                findNavController().popBackStack(R.id.navigation_settings_fragment, false)
            }
        }
    }

    private fun setResult(value: Boolean) {
        setFragmentResult(
            KEY_IS_POPPED,
            bundleOf(
                KEY_FRAG_DATA to value
            )
        )
    }
    //endregion

    override fun onDestroy() {
        super.onDestroy()
        RxHelper.unsubscribe(
            deleteCredsDisposable.get(),
            deletePinDisposable.get(),
            savingDisposable.get(),
            skipPinDisposable.get()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_FLOW = "flow"
        const val FLOW_CREATE = 0
        const val FLOW_SETTINGS = 1
        const val FLOW_AUTHENTICATE = 2
        const val KEY_PIN_CODE = "pin_code"
    }
}