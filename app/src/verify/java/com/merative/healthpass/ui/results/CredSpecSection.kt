package com.merative.healthpass.ui.results

import android.view.View
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.merative.healthpass.R
import com.merative.healthpass.databinding.SimpleRecyclerHeaderBinding
import com.merative.healthpass.ui.common.recyclerView.BaseSection
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class CredSpecSection(
    private val headerString: CharSequence
) : BaseSection<String>(
    SectionParameters.builder()
        .itemResourceId(R.layout.simple_recycler_header)
        .headerResourceId(R.layout.simple_recycler_header)
        .build()
) {
    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<String>(view)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        SimpleRecyclerHeaderBinding.bind(holder.itemView)
            .apply {
                srhText.text = dataList[position]
                dividerLine.isGone = isLastItem(position)
                adjustBackgroundForSection(root, position)
            }
    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<String>(view)

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        ViewUtils.bindSimpleHeader(
            holder.itemView,
            headerString,
            false
        )
    }
}