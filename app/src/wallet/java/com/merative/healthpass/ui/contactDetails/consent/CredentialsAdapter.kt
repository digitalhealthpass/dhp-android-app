package com.merative.healthpass.ui.contactDetails.consent

import android.view.View
import androidx.core.view.isVisible
import com.google.android.material.checkbox.MaterialCheckBox
import com.merative.healthpass.R
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.recyclerView.RecyclerViewBaseAdapter
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.ui.common.recyclerView.ViewUtils

class CredentialsAdapter(
    private val isSelectingEnabled: Boolean
) : RecyclerViewBaseAdapter<Package>() {
    //use it for read only
    var selectedPackages = ArrayList<Package>()

    override fun getLayoutResId(viewType: Int): Int = R.layout.consent_credential_item

    override fun bindData(
        holder: SimpleViewHolder<Package>,
        model: Package,
        position: Int
    ) {
        ViewUtils.adjustCredentialView(holder.itemView, model, false)

        holder.itemView.apply {
            val checkBox = findViewById<MaterialCheckBox>(R.id.checkbox)
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = selectedPackages.contains(model)

            checkBox.apply {
                isVisible = isSelectingEnabled

                setOnCheckedChangeListener { buttonView, isChecked ->
                    clickedEvent.onNext(buttonView to model)
                }
            }

            setOnClickListener {
                checkBox.isChecked = !checkBox.isChecked
            }

            val divider = findViewById<View>(R.id.divider_line)
            divider.isVisible = position < itemCount - 1

        }

        holder.itemView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
}