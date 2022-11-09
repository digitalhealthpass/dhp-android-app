package com.merative.healthpass.ui.contactDetails.associatedData

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.merative.healthpass.R
import com.merative.healthpass.models.Record
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.recyclerView.BaseSection
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class RecordSection(
    headerString: CharSequence,
) : BaseSection<Record>(
    SectionParameters.builder()
        .itemResourceId(R.layout.item_record)
        .headerResourceId(R.layout.simple_recycler_header)
        .build()
) {
    init {
        tag = headerString.toString()
    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<String>(view)

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        ViewUtils.bindSimpleHeader(
            holder.itemView,
            tag ?: "",
            false
        )
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<Package>(view)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = dataList[position]
        holder.itemView.apply {
            val buttonRecord = findViewById<MaterialButton>(R.id.button_record)
            buttonRecord.setOnClickListener { clickEvents.onNext(model) }
            buttonRecord.text = resources.getString(R.string.associated_data_record, position + 1)
        }
    }
}