package com.merative.healthpass.ui.organizations_list

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.merative.healthpass.R
import com.merative.healthpass.extensions.getInitials
import com.merative.healthpass.extensions.setImageFromBase64
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.recyclerView.BaseSection
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import com.merative.watson.healthpass.verifiablecredential.models.credential.credentialSubjectType
import com.merative.watson.healthpass.verifiablecredential.models.credential.getOrgName
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters

class OrganizationsListSection(
    private val headerString: String
) : BaseSection<Package>(
    SectionParameters.builder()
        .itemResourceId(R.layout.credential_list_item)
        .headerResourceId(R.layout.simple_recycler_header)
        .build()
) {

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<Package>(view)

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.apply {
            val aPackage = dataList[position]

            setOnClickListener { clickEvents.onNext(aPackage) }
            adjustOrgCard(this, aPackage, true)

            if (aPackage.isSelected) {
                findViewById<TextView>(R.id.selected_textview).isVisible = true
                findViewById<ImageView>(R.id.goto_imageview).isVisible = false
            } else {
                findViewById<TextView>(R.id.selected_textview).isVisible = false
                findViewById<ImageView>(R.id.goto_imageview).isVisible = true
            }

            adjustBackgroundForSection(this, position)
        }
    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder =
        SimpleViewHolder<String>(view)

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        ViewUtils.bindSimpleHeader(
            holder.itemView,
            headerString + " (${dataList.size})",
            false
        )
    }

    companion object {
        fun adjustOrgCard(
            view: View,
            aPackage: Package,
            showDivider: Boolean
        ) {
            view.apply {
                findViewById<TextView>(R.id.wallet_name_textview).text =
                    aPackage.verifiableObject.credential!!.getOrgName()
                findViewById<TextView>(R.id.type_textview).text =
                    aPackage.verifiableObject.credential!!.credentialSubjectType
                val expDateString = aPackage.verifiableObject.credential!!.getFormattedExpiryDate()
                val exp =
                    context.getString(
                        if (aPackage.verifiableObject.credential!!.isExpired())
                            R.string.result_expiredDate
                        else
                            R.string.result_expiresDate
                    ) + " $expDateString"
                findViewById<TextView>(R.id.wallet_expiration_date_textview).text = exp

                findViewById<ImageView>(R.id.wallet_credential_imageview).setImageFromBase64(
                    aPackage.issuerMetaData?.metadata?.logo,
                    aPackage.verifiableObject.credential?.getOrgName()?.getInitials()
                )

                findViewById<View>(R.id.divider_line).isVisible = showDivider
            }
        }
    }
}