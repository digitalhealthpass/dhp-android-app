package com.merative.healthpass.ui.registration.details

import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import com.merative.healthpass.R
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.extensions.getDrawableCompat
import com.merative.healthpass.extensions.isNotNullOrEmpty
import com.merative.healthpass.extensions.splitCamelCase
import com.merative.healthpass.ui.common.recyclerView.RecyclerViewBaseAdapter
import com.merative.healthpass.ui.common.recyclerView.SimpleViewHolder
import com.merative.healthpass.utils.Utils
import com.merative.healthpass.utils.schema.*

class RegistrationDetailsAdapter(
    private val viewModel: RegistrationDetailsVM
) : RecyclerViewBaseAdapter<Field>() {

    override fun getItemViewType(position: Int): Int {
        val model = dataList[position]
        return when {
            //order is important
            model.hasAList() -> VIEW_TYPE_LIST
            model.isBoolean() -> VIEW_TYPE_BOOLEAN
            model.isString() -> VIEW_TYPE_STRING
            else -> throw IllegalStateException("This field type is not handled $position")
        }
    }

    override fun getLayoutResId(viewType: Int): Int {
        return when (viewType) {
            //order is important
            VIEW_TYPE_LIST -> R.layout.ac_drop_down_item
            VIEW_TYPE_BOOLEAN -> R.layout.ac_switch_item
            VIEW_TYPE_STRING -> R.layout.ac_edit_item
            else -> AppConstants.INVALID_RES_ID
        }
    }

    override fun bindData(holder: SimpleViewHolder<Field>, model: Field, position: Int) {
        when {
            //order is important
            model.hasAList() -> bindArray(holder, model, position)
            model.isBoolean() -> bindBoolean(holder, model, position)
            model.isString() -> bindText(holder, model, position)
            else -> throw IllegalStateException("This field type is not handled $model")
        }
    }

    private fun bindArray(holder: SimpleViewHolder<Field>, model: Field, position: Int) {
        val txtTitle = holder.itemView.findViewById<TextView>(R.id.title)
        val titleText = model.getLastPathPart().splitCamelCase().capitalize()
        txtTitle.text = titleText

        val til = holder.itemView.findViewById<TextInputLayout>(R.id.til)
        if (Utils.StringUtils.isAllValid(model.description)) til.editText?.hint = model.description
        else if (Utils.StringUtils.isAllValid(titleText)) til.editText?.hint = titleText

        adjustEditTextValidation(holder, til.editText, model)

        //user the new value if exists in the map, otherwise, fall back to default Field Value
        val valueToSet = viewModel.getUsableStringFromMap(model)

        til.editText?.apply {
            setText(valueToSet)
            showSoftInputOnFocus = false
            isCursorVisible = false
            isFocusable = false
        }

        til.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                clickedEvent.onNext(v to model)
            }
            true
        }
        til.editText?.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                clickedEvent.onNext(v to model)
            }
            true
        }
    }

    private fun bindText(holder: SimpleViewHolder<Field>, model: Field, position: Int) {
        val txtTitle = holder.itemView.findViewById<TextView>(R.id.title)
        txtTitle.text = model.getLastPathPart().splitCamelCase().capitalize()

        val til = holder.itemView.findViewById<TextInputLayout>(R.id.til)
        if (Utils.StringUtils.isAllValid(model.description)) til.editText?.hint = model.description
        else if (Utils.StringUtils.isAllValid(txtTitle.text.toString())) til.editText?.hint = txtTitle.text.toString()

        adjustEditTextValidation(holder, til.editText, model)

        //user the new value if exists in the map, otherwise, fall back to default Field Value
        val valueToSet = viewModel.getUsableStringFromMap(model)
        til.editText?.setText(valueToSet)
        if (model.editable) {
            til.editText?.inputType = InputType.TYPE_CLASS_TEXT
        } else {
            til.editText?.inputType = InputType.TYPE_NULL
        }

        holder.itemView.setOnClickListener(null)
    }

    //this was requested as a feature but so far there is no valid case
    private fun bindBoolean(holder: SimpleViewHolder<Field>, model: Field, position: Int) {
        val txtTitle = holder.itemView.findViewById<TextView>(R.id.title)
        txtTitle.text = model.getLastPathPart().splitCamelCase().capitalize()

        val switchMaterial = holder.itemView.findViewById<SwitchMaterial>(R.id.switchWidget)
        switchMaterial.text = viewModel.getUsableStringFromMap(model)
        //hint appears if value is empty
        switchMaterial.hint = model.description

        switchMaterial.setOnCheckedChangeListener(null)
        switchMaterial.isChecked = model.value.toString().toBoolean()

        switchMaterial.setOnCheckedChangeListener { _, isChecked ->
            model.value = isChecked.toString()
            viewModel.fieldsMap[model] = isChecked
            validateForm()
        }
        viewModel.fieldsMap[model] = switchMaterial.isChecked
    }

    private fun adjustEditTextValidation(
        holder: SimpleViewHolder<Field>,
        originEditText: EditText?,
        model: Field
    ) {
        holder.textListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(it: Editable?) {
                originEditText?.let { editText ->
                    val drawable = if (it.isNotNullOrEmpty()) {
                        editText.context.getDrawableCompat(R.drawable.ic_verified_24px)
                    } else {
                        null
                    }

                    editText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        null,
                        null,
                        drawable,
                        null
                    )
                    if (model.editable) {
                        viewModel.fieldsMap[model] = it?.toString()
                    }

                    validateForm()
                }
            }
        }
        originEditText?.addTextChangedListener(holder.textListener)
    }

    override fun setViewClickListener(view: View, model: Field, position: Int) {
    }

    override fun onViewDetachedFromWindow(holder: SimpleViewHolder<Field>) {
        super.onViewDetachedFromWindow(holder)
        holder.removeTextWatcher()
    }

    override fun onViewAttachedToWindow(holder: SimpleViewHolder<Field>) {
        super.onViewAttachedToWindow(holder)
        val model = holder.model
        val til = holder.itemView.findViewById<TextInputLayout>(R.id.til)
        if (til != null && model != null) {
            adjustEditTextValidation(holder, til.editText, model)
        }
    }

    private fun validateForm() = viewModel.validateForm()

    companion object {
        const val VIEW_TYPE_BOOLEAN = 1
        const val VIEW_TYPE_STRING = 2
        const val VIEW_TYPE_LIST = 3
    }
}