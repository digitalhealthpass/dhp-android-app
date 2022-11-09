package com.merative.healthpass.ui.common.baseViews

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.merative.healthpass.R
import com.merative.healthpass.ui.common.recyclerView.BaseSection
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class StringSection : BaseSection<CharSequence>(
    SectionParameters.builder()
        .itemResourceId(R.layout.string_item)
        .build()
) {
    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<String>(view)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.textView)
            .apply {
                text = dataList[position]
            }
    }
}