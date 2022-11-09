package com.merative.healthpass.ui.contactDetails.details

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.merative.healthpass.R
import com.merative.healthpass.extensions.isEnabledAlpha
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.recyclerView.BaseSection
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class UDCredentialSection(
    @StringRes private val stringID: Int,
    @DrawableRes private val imageID: Int,
    private val shouldDisableUD: Boolean,
) : BaseSection<Package>(
    SectionParameters.builder()
        .itemResourceId(R.layout.downloaded_passes_rv_item)
        .footerResourceId(R.layout.simple_text_with_icon_rv_item)
        .build()
) {
    private val footerClickEvents = PublishSubject.create<Unit>()

    override fun getFooterViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<Any>(view)

    override fun onBindFooterViewHolder(holder: RecyclerView.ViewHolder) {
        holder.itemView.apply {
            findViewById<TextView>(R.id.twi_txt).text = holder.itemView.context.getString(stringID)
            findViewById<ImageView>(R.id.twi_img).setImageResource(imageID)
            if (shouldDisableUD) {
                isEnabledAlpha = false
            } else {
                isEnabledAlpha = true
                setOnClickListener { footerClickEvents.onNext(Unit) }
            }
        }
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<Package>(view)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.apply {
            val model = dataList[position]
            ViewUtils.adjustCredentialView(this, model, false)
            setOnClickListener { clickEvents.onNext(model) }
        }
    }

    fun listenToFooterClickEvents(): Observable<Unit> = footerClickEvents
}