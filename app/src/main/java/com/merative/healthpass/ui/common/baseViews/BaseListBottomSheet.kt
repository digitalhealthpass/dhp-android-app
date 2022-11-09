package com.merative.healthpass.ui.common.baseViews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.merative.healthpass.R
import com.merative.healthpass.common.App
import com.merative.healthpass.extensions.addIfNotExist
import com.merative.healthpass.extensions.isNotNullOrEmpty
import com.merative.healthpass.extensions.orEmpty
import com.merative.healthpass.ui.mainActivity.MainActivity
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import javax.inject.Inject

abstract class BaseListBottomSheet : BottomSheetDialogFragment() {
    private lateinit var loadingDialog: LoadingDialog

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val sectionAdapter = SectionedRecyclerViewAdapter()
    protected lateinit var section: StringSection

    //region onCreate
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().applicationContext as App).appComponent
            .inject(this)

        loadingDialog = LoadingDialog(activity)
        lifecycle.addObserver(loadingDialog)
    }

    @CallSuper
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.frag_list_bottom, container, false)
    }
    //endregion

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = arguments?.getCharSequenceArrayList(KEY_MAIN_LIST).orEmpty()
        val btnCancel = arguments?.getCharSequence(KEY_CANCEL)

        view.findViewById<View>(R.id.list_btn_cancel_container)
            .apply {
                isVisible = btnCancel.isNotNullOrEmpty()
            }

        view.findViewById<RecyclerView>(R.id.list_recycler)
            .apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = sectionAdapter
                section = StringSection()
                section.setDataList(list)
                sectionAdapter.addIfNotExist(section)
            }
    }

    //region helpers
    protected fun <VM : ViewModel> createViewModel(modelClass: Class<VM>): VM {
        return ViewModelProvider(this, viewModelFactory).get(modelClass)
    }

    protected fun showLoading(show: Boolean) {
        (activity as? MainActivity)?.run {
            runOnUiThread { loadingDialog.showLoading(show) }
        }
    }
    //endregion

    //region handle back
    fun setResult(isJobFinished: Boolean) {
        setFragmentResult(
            KEY_IS_DISMISSED,
            bundleOf(
                KEY_RESULT_DATA to isJobFinished
            )
        )
        dismiss()
    }
    //endregion

    override fun onDestroy() {
        super.onDestroy()
        showLoading(false)
    }

    companion object {
        const val KEY_MAIN_LIST = "main_list"
        const val KEY_CANCEL = "cancel"

        const val KEY_IS_DISMISSED = "list_isDismissed"
        const val KEY_RESULT_DATA = "list_result_data"
    }
}