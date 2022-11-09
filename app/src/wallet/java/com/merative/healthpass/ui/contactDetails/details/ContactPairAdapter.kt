package com.merative.healthpass.ui.contactDetails.details

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import com.merative.healthpass.R
import com.merative.healthpass.ui.common.recyclerView.RecyclerViewBaseAdapter
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder

class ContactPairAdapter : RecyclerViewBaseAdapter<Pair<String, CharSequence>>() {
    override fun getLayoutResId(viewType: Int): Int = R.layout.simple_info_recycler_item_view

    override fun bindData(
        holder: SimpleViewHolder<Pair<String, CharSequence>>,
        model: Pair<String, CharSequence>,
        position: Int
    ) {
        holder.itemView.let { view ->
            view.findViewById<TextView>(R.id.txt_title).text = model.first
            view.findViewById<TextView>(R.id.txt_value).text = model.second

            view.findViewById<Button>(R.id.btn_copy).isVisible = model.first.equals(
                holder.itemView.context.getString(R.string.public_key_label),
                true
            )
            view.findViewById<View>(R.id.divider_line).isVisible = position != itemCount - 1
        }
    }

    override fun setViewClickListener(
        view: View,
        model: Pair<String, CharSequence>,
        position: Int
    ) {
    }
}