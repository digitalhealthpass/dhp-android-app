package com.merative.healthpass.ui.results

import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.merative.healthpass.R
import com.merative.healthpass.databinding.SimpleRecyclerHeaderBinding
import com.merative.healthpass.extensions.getColorCompat
import com.merative.healthpass.extensions.isNotNullOrEmpty
import com.merative.healthpass.extensions.orValue
import com.merative.healthpass.ui.common.recyclerView.BaseSection
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class IssuerSection(
    private val headerString: CharSequence
) : BaseSection<String?>(
    SectionParameters.builder()
        .itemResourceId(R.layout.simple_recycler_header)
        .headerResourceId(R.layout.simple_recycler_header)
        .build()
) {
    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<String?>(view)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context

        SimpleRecyclerHeaderBinding.bind(holder.itemView)
            .apply {
                val ssb = SpannableStringBuilder()
                    .bold { append(dataList[position].orValue(context.getString(R.string.result_unknown))) }

                ssb.appendLine()
                if (dataList[position].isNotNullOrEmpty()) {
                    ssb.color(context.getColorCompat(R.color.switch_on)) { append(context.getString(R.string.result_issuerRecognized)) }
                } else {
                    ssb.color(context.getColorCompat(R.color.systemOrange)) { append(context.getString(R.string.result_issuerNotRecognized)) }
                }

                srhText.text = ssb
                dividerLine.isGone = isLastItem(position)
                adjustBackgroundForSection(root, position)
            }
    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<String?>(view)

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        ViewUtils.bindSimpleHeader(
            holder.itemView,
            headerString,
            false
        )
    }
}