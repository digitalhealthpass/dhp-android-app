package com.merative.healthpass.ui.credential.sharing

import android.view.View
import androidx.core.view.isVisible
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.JsonElement
import com.merative.healthpass.R
import com.merative.healthpass.extensions.orValue
import com.merative.healthpass.ui.common.recyclerView.RecyclerViewBaseAdapter
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.watson.healthpass.verifiablecredential.extensions.getStringOrNull

class ObfuscatedFieldAdapter(private val obfuscatedVM: ObfuscationSettingsVM) :
    RecyclerViewBaseAdapter<JsonElement>() {

    override fun getLayoutResId(viewType: Int): Int = R.layout.recycler_switch_item

    override fun bindData(
        holder: SimpleViewHolder<JsonElement>,
        model: JsonElement,
        position: Int
    ) {
        holder.itemView.let { view ->
            view.findViewById<SwitchMaterial>(R.id.switch_item).apply {
                text = model.asJsonObject.getStringOrNull("path")
                    ?.substringAfterLast('.')
                    .orEmpty().capitalize()

                setOnCheckedChangeListener(null)
                isChecked = obfuscatedVM.obfuscationObjectList[model].orValue(false)
                setOnCheckedChangeListener { _, isChecked ->
                    obfuscatedVM.obfuscationObjectList[model] = isChecked
                }
            }
            view.findViewById<View>(R.id.divider_line).isVisible = position != itemCount - 1
        }
    }
}