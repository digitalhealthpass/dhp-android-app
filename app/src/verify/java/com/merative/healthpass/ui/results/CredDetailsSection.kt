package com.merative.healthpass.ui.results

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.merative.healthpass.R
import com.merative.healthpass.ui.common.recyclerView.BaseSection
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class CredDetailsSection(
    private val headerString: CharSequence
) : BaseSection<Pair<String, String>>(
    SectionParameters.builder()
        .itemResourceId(R.layout.simple_recycler_label_value_item)
        .headerResourceId(R.layout.simple_recycler_header)
        .build()
) {
    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<Pair<String, String>>(view)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        ViewUtils.bindSimpleView(
            holder.itemView,
            dataList[position].first,
            dataList[position].second, isLastItem(position),
            addPadding = true
        )
        holder.itemView.apply {
            adjustBackgroundForSection(this, position)
        }
    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<Pair<String, String>>(view)

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        ViewUtils.bindSimpleHeader(
            holder.itemView,
            headerString,
            false
        )
    }
}