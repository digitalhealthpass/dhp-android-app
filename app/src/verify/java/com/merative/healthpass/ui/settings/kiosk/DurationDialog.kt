package com.merative.healthpass.ui.settings.kiosk

import android.view.View
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.merative.healthpass.R
import com.merative.healthpass.models.Duration
import com.merative.healthpass.ui.common.baseViews.BaseDialog


class DurationDialog : BaseDialog() {

    override val titleId: Int
        get() = R.string.kiosk_mode_auto_dismiss_duration

    override val layoutId: Int
        get() = R.layout.dialog_duration

    private lateinit var adapter: DurationAdapter

    override fun addCustomBehavior(view: View?) {
        val recyclerView = view?.findViewById<RecyclerView>(R.id.durationRecyclerView)
        adapter = DurationAdapter(this::onCategorySelected)
        recyclerView?.adapter = adapter

        val list = arguments?.get(DURATION_KEY) as List<Duration>
        adapter.addItems(list)
    }

    private fun onCategorySelected(duration: Duration) {
        (targetFragment ?: activity).let {
            (it as DurationDialogListener).onDurationItemClick(duration)
            dismiss()
        }
    }

    interface DurationDialogListener {

        fun onDurationItemClick(input: Duration)
    }

    companion object {

        const val DURATION_FRAGMENT_TAG = "category_fragment_tag"
        private const val DURATION_KEY = "category_key"

        fun newInstance(duration: List<Duration>) =
            DurationDialog().apply {
                arguments = bundleOf(DURATION_KEY to duration)
            }
    }
}