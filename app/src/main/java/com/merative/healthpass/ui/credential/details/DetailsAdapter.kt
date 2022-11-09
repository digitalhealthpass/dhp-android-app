package com.merative.healthpass.ui.credential.details

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.merative.healthpass.R
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.extensions.isNotNullOrEmpty
import com.merative.healthpass.ui.common.recyclerView.BaseSection
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import com.merative.healthpass.utils.schema.Field
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import java.util.*

class DetailsAdapter(
    private val headerString: CharSequence,
    private val showHeader: Boolean = true
) : BaseSection<Field>(
    SectionParameters.builder()
        .itemResourceId(R.layout.simple_recycler_label_value_item)
        .headerResourceId(R.layout.simple_recycler_header)
        .build()
) {
    init {
        tag = headerString.toString()
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<Pair<String, String>>(view)

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<Pair<String, String>>(view)

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        ViewUtils.bindSimpleHeader(
            holder.itemView,
            headerString,
            showHeader = showHeader,
            showDivider = false
        )
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = dataList[position]
        val (leftText, rightText) = model.getUsableValue(Locale.getDefault())
        ViewUtils.bindSimpleView(
            view = holder.itemView,
            leftText = leftText,
            rightText = rightText,
            showDivider = !isLastItem(position),
            drawResId = (if (model.obfuscatedValueObject != null) {
                R.drawable.ic_lock_open
            } else if (model.obfuscatedValueObject == null && model.isObfuscated) {
                R.drawable.ic_lock
            } else if (model.recordStatus.isNotNullOrEmpty()) {
                if (model.recordStatus.toBoolean()) {
                    R.drawable.checkmark_seal_green
                } else {
                    R.drawable.x_mark_seal_red
                }
            } else {
                AppConstants.INVALID_RES_ID
            }),
            imageDescription = when {
                model.obfuscatedValueObject != null -> holder.itemView.context.getString(R.string.unlocked)
                (model.obfuscatedValueObject == null && model.isObfuscated) ->holder.itemView.context.getString(R.string.locked)
                model.recordStatus.isNotNullOrEmpty() -> holder.itemView.context.getString(R.string.record_status)
                else -> ""
            },
            addPadding = true
        )
        holder.itemView.apply {
            adjustBackgroundForSection(this, position)
        }
    }
}