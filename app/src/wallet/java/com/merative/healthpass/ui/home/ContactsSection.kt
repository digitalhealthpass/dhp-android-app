package com.merative.healthpass.ui.home

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.merative.healthpass.R
import com.merative.healthpass.extensions.isNotNullOrEmpty
import com.merative.healthpass.extensions.setImageFromBase64
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.recyclerView.BaseSection
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import com.merative.watson.healthpass.verifiablecredential.extensions.getStringOrNull
import com.merative.watson.healthpass.verifiablecredential.models.credential.getFirstPiiController
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class ContactsSection : BaseSection<ContactPackage>(
    SectionParameters.builder()
        .itemResourceId(R.layout.wallet_item_contact)
        .headerResourceId(R.layout.simple_recycler_header)
        .build()
) {
    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<String>(view)

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        ViewUtils.bindSimpleHeader(
            holder.itemView,
            tag + " (${dataList.size})",
            false
        )
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<Package>(view)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = dataList[position]
        holder.itemView.apply {
            setOnClickListener { clickEvents.onNext(model) }

            val contactName: String = model.getContactName()

            findViewById<ImageView>(R.id.imgLeft).setImageFromBase64(
                model.profilePackage?.issuerMetaData?.metadata?.logo, null
            )

            val subTitle: String = if (model.isDefaultPackage()) {
                ""//model.profilePackage?.credential?.credentialSubject?.getStringOrNull("contact").orEmpty()
            } else {
                model.profilePackage?.verifiableObject?.credential
                    ?.getFirstPiiController()?.getStringOrNull("contact").orEmpty()
            }
            findViewById<TextView>(R.id.title).text = contactName

            findViewById<TextView>(R.id.subTitle).apply {
                isVisible = subTitle.isNotNullOrEmpty()
                text = subTitle
            }
        }
    }
}