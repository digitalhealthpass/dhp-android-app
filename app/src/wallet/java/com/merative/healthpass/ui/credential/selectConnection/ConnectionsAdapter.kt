package com.merative.healthpass.ui.credential.selectConnection

import android.widget.ImageView
import androidx.core.view.isVisible
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textview.MaterialTextView
import com.merative.healthpass.R
import com.merative.healthpass.extensions.setImageFromBase64
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.ui.common.recyclerView.RecyclerViewBaseAdapter
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder

class ConnectionsAdapter : RecyclerViewBaseAdapter<ContactPackage>() {
    //use it for read only
    var selectedPackage = ArrayList<ContactPackage>()
    private var mSelectedItem = -1

    override fun getLayoutResId(viewType: Int): Int = R.layout.cards_adapter

    override fun bindData(holder: SimpleViewHolder<ContactPackage>, model: ContactPackage, position: Int) {
        holder.itemView.apply {
            val radioButton = findViewById<MaterialRadioButton>(R.id.radioButton)
            radioButton.setOnCheckedChangeListener(null)
            radioButton.isChecked = selectedPackage.contains(model)

            radioButton.apply {
                isVisible = true

                setOnCheckedChangeListener { buttonView, _ ->
                    clickedEvent.onNext(buttonView to model)
                }
            }

            radioButton.isChecked = mSelectedItem == position

            setOnClickListener {
                position.also { mSelectedItem = it }
                notifyDataSetChanged()
            }

            findViewById<MaterialTextView>(R.id.title).text = model.getContactName()
            findViewById<ImageView>(R.id.imgRight).setImageFromBase64(
                model.profilePackage?.issuerMetaData?.metadata?.logo, null
            )
        }
    }
}