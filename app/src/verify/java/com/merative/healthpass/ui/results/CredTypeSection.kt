package com.merative.healthpass.ui.results

import android.view.View
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.merative.healthpass.R
import com.merative.healthpass.databinding.SimpleRecyclerHeaderDescriptionBinding
import com.merative.healthpass.models.specificationconfiguration.SpecificationConfiguration
import com.merative.healthpass.ui.common.recyclerView.BaseSection
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import com.merative.healthpass.utils.Utils
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class CredTypeSection(
    private val headerString: CharSequence
) : BaseSection<SpecificationConfiguration>(
    SectionParameters.builder()
        .itemResourceId(R.layout.simple_recycler_header_description)
        .headerResourceId(R.layout.simple_recycler_header)
        .build()
) {
    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<SpecificationConfiguration>(view)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        SimpleRecyclerHeaderDescriptionBinding.bind(holder.itemView)
            .apply {
                val title = dataList[position].credentialCategoryDisplayValue
                val description = dataList[position].description
                srhText.text = title
                srhText.visibility = if (Utils.StringUtils.isAllValid(title)) View.VISIBLE else View.GONE
                descriptionTextView.text = description
                descriptionTextView.visibility = if (Utils.StringUtils.isAllValid(description)) View.VISIBLE else View.GONE
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