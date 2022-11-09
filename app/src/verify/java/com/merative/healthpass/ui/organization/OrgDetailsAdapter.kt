package com.merative.healthpass.ui.organization

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.merative.healthpass.R
import com.merative.healthpass.extensions.removeExtraQuote
import com.merative.healthpass.extensions.splitCamelCase
import com.merative.healthpass.ui.common.recyclerView.RecyclerViewBaseAdapter
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.utils.schema.Field

class OrgDetailsAdapter : RecyclerViewBaseAdapter<Field>() {
    override fun getLayoutResId(viewType: Int): Int = R.layout.simple_info_recycler_item_view

    override fun bindData(
        holder: SimpleViewHolder<Field>,
        model: Field,
        position: Int
    ) {
        holder.itemView.let { view ->
            view.findViewById<TextView>(R.id.txt_title).text =
                model.path.splitCamelCase().capitalize().removeExtraQuote()
            view.findViewById<TextView>(R.id.txt_value).text =
                model.value.toString().removeExtraQuote()

            view.findViewById<View>(R.id.divider_line).isVisible = position != itemCount - 1
        }

        holder.itemView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
}