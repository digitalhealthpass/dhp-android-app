package com.merative.healthpass.ui.settings.kiosk

import android.widget.RadioButton
import com.merative.healthpass.R
import com.merative.healthpass.models.Duration
import com.merative.healthpass.ui.common.recyclerView.RecyclerViewBaseAdapter
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder


class DurationAdapter(private val onDurationSelected: (Duration) -> Unit) :
    RecyclerViewBaseAdapter<Duration>() {

    override fun getLayoutResId(viewType: Int) = R.layout.item_duration

    override fun bindData(holder: SimpleViewHolder<Duration>, model: Duration, position: Int) {
        val view = holder.itemView

        val itemRadioButton = view.findViewById<RadioButton>(R.id.itemRadioButton)

        itemRadioButton.text =
            holder.resources.getString(R.string.kiosk_mode_auto_dismiss_sec, model.interval)

        view.setOnClickListener { onDurationSelected.invoke(model) }
    }
}