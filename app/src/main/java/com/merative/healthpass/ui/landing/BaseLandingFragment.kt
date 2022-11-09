package com.merative.healthpass.ui.landing

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.os.bundleOf
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragLandingBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.ui.common.baseViews.BaseBottomSheet
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.utils.DeviceUtils
import com.merative.healthpass.utils.RxHelper
import com.merative.watson.healthpass.BuildConfig
import io.reactivex.rxjava3.disposables.CompositeDisposable

abstract class BaseLandingFragment : BaseFragment() {
    override val homeBtnEnabled: Boolean
        get() = false

    private var _binding: FragLandingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    protected val binding get() = _binding!!

    protected lateinit var viewModel: LandingViewModel

    private val deleteDisposable = CompositeDisposable()

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(LandingViewModel::class.java)

        binding.topContainer.setOnLongClickListener {
            activity?.showDebugDialog()
            true
        }

        if (viewModel.isTimeToResetCache()) {
            showLoading(true)
            deleteDisposable.add(
                viewModel.resetCache()
                    .doFinally { showLoading(false) }
                    .subscribe({ }, rxError("Failed to reset Cache"))
            )
        }

        if (arguments?.getBoolean(KEY_DISABLE_UI) != true) {
            updateContinueButtonAvailability(true)
        }
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        showActionBar(false)
        handleRootIfNeeded()
        initUI()
        setFragmentResultListener(KEY_IS_POPPED) { _, bundle ->
            val shouldContinue = bundle.getBoolean(KEY_FRAG_DATA).or(false)
            if (shouldContinue) {
                clearFragmentResult(KEY_IS_POPPED)
                determineDestination()
            } else {
                updateContinueButtonAvailability(true)
            }
        }
        setFragmentResultListener(BaseBottomSheet.KEY_IS_DISMISSED) { _, bundle ->
            val shouldContinue = bundle.getBoolean(BaseBottomSheet.KEY_RESULT_DATA).or(false)
            if (shouldContinue) {
                clearFragmentResult(BaseBottomSheet.KEY_IS_DISMISSED)
                onDialogDismissed()
            } else {
                updateContinueButtonAvailability(true)
            }

        }
        if (hasDeepLink()) determineDestination()
    }

    protected open fun onDialogDismissed() {
        //navigation was being called too fast and needs a bit of delay to get triggered correctly
        Handler().postDelayed({
            determineDestination()
        }, 50L)
    }

    private fun initUI() {
        binding.titleTextview.text = getString(LandingConstants.APP_LONG_NAME_ID)
        val versionString = getString(R.string.profile_build_title) + " " + buildVersion
        binding.splashBuildTextview.text = versionString

        binding.splashPpButton.setOnClickListener {
            findNavController().navigate(R.id.global_action_to_privacy_frag)
        }
        binding.splashContinueButton.setOnClickListener {
            determineDestination()
        }
    }

    private fun handleRootIfNeeded() {
        val isRooted = DeviceUtils.isProhibitedRoot(requireContext())
        if (isRooted) {
            activity?.showDialog(
                getString(R.string.launch_root_title),
                getString(R.string.launch_root_message),
                getString(R.string.button_title_ok)
            ) {
                if (!BuildConfig.DEBUG) {
                    activity?.finishAffinity()
                }
            }
        }
    }

    @CallSuper
    protected open fun determineDestination() {
        updateContinueButtonAvailability(false)
        arguments?.putBoolean(KEY_DISABLE_UI, true) ?: run {
            arguments = bundleOf(KEY_DISABLE_UI to true)
        }
    }

    private fun updateContinueButtonAvailability(active: Boolean) {
        binding.splashContinueButton.isClickable = active
        binding.splashContinueButton.isFocusable = active
        binding.splashContinueButton.isEnabled = active
    }

    protected abstract fun hasDeepLink(): Boolean

    override fun onDestroyView() {
        super.onDestroyView()
        RxHelper.unsubscribe(deleteDisposable)
        _binding = null
    }

    companion object {
        const val KEY_DISABLE_UI = "disable_ui"
    }
}