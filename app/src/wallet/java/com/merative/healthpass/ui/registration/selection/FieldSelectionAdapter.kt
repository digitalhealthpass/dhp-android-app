package com.merative.healthpass.ui.registration.selection

import android.widget.CheckBox
import android.widget.RadioButton
import com.merative.healthpass.R
import com.merative.healthpass.ui.common.recyclerView.RecyclerViewBaseAdapter
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.utils.schema.Field
import com.merative.healthpass.utils.schema.isArray

class FieldSelectionAdapter(
    private val field: Field,
    private val selectedList: ArrayList<Any>
) : RecyclerViewBaseAdapter<Any>() {

    override fun getLayoutResId(viewType: Int): Int {
        return when {
            field.isArray() -> R.layout.ac_multi_select_item
            else -> R.layout.ac_single_select_item
        }
    }

    override fun bindData(holder: SimpleViewHolder<Any>, model: Any, position: Int) {
        if (field.isArray()) {
            bindCheckBox(holder, model, position)
        } else {
            bindRadioButton(holder, model, position)
        }
    }

    private fun bindCheckBox(holder: SimpleViewHolder<Any>, model: Any, position: Int) {
        holder.itemView.findViewById<CheckBox>(R.id.checkbox).apply {
            text = model.toString()
            setOnCheckedChangeListener(null)
            isChecked = selectedList.contains(model)
            setOnCheckedChangeListener { buttonView, isChecked ->
                clickedEvent.onNext(buttonView to model)
            }
        }
    }

    private fun bindRadioButton(holder: SimpleViewHolder<Any>, model: Any, position: Int) {
        holder.itemView.findViewById<RadioButton>(R.id.radio).apply {
            text = model.toString()
            setOnCheckedChangeListener(null)
            isChecked = selectedList.contains(model) || selectedList.contains(model.toString())
            setOnCheckedChangeListener { buttonView, isChecked ->
                clickedEvent.onNext(buttonView to model)
            }
        }
    }
}