package com.merative.healthpass.ui.common.baseViews

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.merative.healthpass.R
import com.merative.healthpass.common.App
import com.merative.healthpass.ui.mainActivity.FlavorVM
import com.merative.healthpass.ui.mainActivity.MainActivity
import com.merative.healthpass.ui.mainActivity.MainActivityVM
import javax.inject.Inject

abstract class BaseBottomSheet : BottomSheetDialogFragment() {
    private lateinit var loadingDialog: LoadingDialog

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    protected val mainActivityVM: MainActivityVM by activityViewModels()

    protected val flavorVM: FlavorVM by activityViewModels { viewModelFactory }

    private var isJobFinished = false

    override fun onStart() {
        super.onStart()
        val containerID = com.google.android.material.R.id.design_bottom_sheet
        val bottomSheet: FrameLayout? = dialog?.findViewById(containerID)
        bottomSheet?.let {
            BottomSheetBehavior.from<FrameLayout?>(it).state =
                BottomSheetBehavior.STATE_EXPANDED
            bottomSheet.layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
        }

        view?.post {
            val params = (view?.parent as? View)?.layoutParams as? (CoordinatorLayout.LayoutParams)
            val behavior = params?.behavior
            val bottomSheetBehavior = behavior as? (BottomSheetBehavior)

            bottomSheetBehavior?.peekHeight =
                view?.measuredHeight ?: Resources.getSystem().displayMetrics.heightPixels
            (bottomSheet?.parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
        }

        loadingDialog = LoadingDialog(activity)
        lifecycle.addObserver(loadingDialog)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().applicationContext as App).appComponent
            .inject(this)
    }

    protected fun <VM : ViewModel> createViewModel(modelClass: Class<VM>): VM {
        return ViewModelProvider(this, viewModelFactory).get(modelClass)
    }

    protected fun showLoading(show: Boolean) {
        (activity as? MainActivity)?.run {
            runOnUiThread { loadingDialog.showLoading(show) }
        }
    }

    protected fun goToWallet() {
        findNavController().navigate(R.id.global_action_pop_to_home)
    }

    //region handle back
    fun setResult(isJobFinished: Boolean) {
        this.isJobFinished = isJobFinished
        dismiss()
    }

    override fun dismiss() {
        super.dismiss()
        setFragmentResult(
            KEY_IS_DISMISSED,
            getResultBundle()
        )
    }

    protected open fun getResultBundle(): Bundle = bundleOf(
        KEY_RESULT_DATA to isJobFinished
    )
    //endregion

    override fun onDestroy() {
        super.onDestroy()
        showLoading(false)
    }

    companion object {
        const val KEY_IS_DISMISSED = "isDismissed"
        const val KEY_RESULT_DATA = "result_data"
    }
}