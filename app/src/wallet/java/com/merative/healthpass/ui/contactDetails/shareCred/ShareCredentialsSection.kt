package com.merative.healthpass.ui.contactDetails.shareCred

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.merative.healthpass.R
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.recyclerView.BaseSection
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class ShareCredentialsSection : BaseSection<Package>(
    SectionParameters.builder()
        .itemResourceId(R.layout.wallet_item)
        .headerResourceId(R.layout.simple_recycler_header)
        .build()
) {
    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<String>(view)

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        ViewUtils.bindSimpleHeader(
            holder.itemView,
            holder.itemView.context.getString(R.string.cards) + " (${dataList.size})",
            false
        )
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<Package>(view)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.setOnClickListener { clickEvents.onNext(dataList[position]) }
        ViewUtils.adjustCredentialView(holder.itemView, dataList[position])

        holder.itemView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
}