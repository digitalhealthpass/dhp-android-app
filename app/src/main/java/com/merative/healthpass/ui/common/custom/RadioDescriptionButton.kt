package com.merative.healthpass.ui.common.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.merative.healthpass.R
import com.merative.healthpass.databinding.BtnRadioDescriptionBinding

class RadioDescriptionButton : ConstraintLayout {

    private lateinit var binding: BtnRadioDescriptionBinding

    //region constructor
    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        binding = BtnRadioDescriptionBinding.inflate(LayoutInflater.from(context), this, true)
//        orientation = VERTICAL

        val minH = context.resources.getDimensionPixelSize(R.dimen.minHeight)
        minimumHeight = minH

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.RadioDescriptionButton,
                0,
                0
            )

            val title = a.getText(R.styleable.RadioDescriptionButton_rdb_title)
            if (!title.isNullOrEmpty()) {
                binding.txtTitle.text = title
            }
            val titleColor =
                a.getInt(R.styleable.RadioDescriptionButton_rdb_title_color, R.color.textLabels)
            binding.txtTitle.setTextColor(context.getColor(titleColor))

            val description = a.getText(R.styleable.RadioDescriptionButton_rdb_description)
            if (!description.isNullOrEmpty()) {
                binding.txtDescription.text = description
            }

            val descriptionColor = a.getInt(
                R.styleable.RadioDescriptionButton_rdb_description_color,
                R.color.subTextLabels
            )
            binding.txtDescription.setTextColor(context.getColor(descriptionColor))

            val showDivider =
                a.getBoolean(R.styleable.RadioDescriptionButton_rdb_show_divider, true)

            if (showDivider) {
                binding.dividerLine.visibility = View.VISIBLE
            } else {
                binding.dividerLine.visibility = View.GONE
            }
        }
    }
    //endregion

    fun setChecked(isChecked: Boolean) {
        if (isChecked) {
            binding.txtTitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                R.drawable.outline_check_24,
                0
            )
        } else {
            binding.txtTitle.setCompoundDrawables(null, null, null, null)
        }
    }

    companion object {
        fun setViewData(
            view: View,
            title: CharSequence,
            description: CharSequence,
            isChecked: Boolean,
            showDivider: Boolean,
            checkAction: (isChecked: Boolean) -> Unit
        ) {
            val txtTitle = view.findViewById<TextView>(R.id.txt_title)
            val txtDescription = view.findViewById<TextView>(R.id.txt_description)
            val divider = view.findViewById<View>(R.id.divider)

            txtTitle.text = title
            if (isChecked) {
                txtTitle.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.outline_check_24,
                    0
                )
            } else {
                txtTitle.setCompoundDrawables(null, null, null, null)
            }

            txtDescription.isVisible = description.isNotBlank()
            txtDescription.text = description

            divider.isVisible = showDivider

            view.setOnClickListener {
                val valueToUse = !isChecked
                if (valueToUse) {
                    txtTitle.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.outline_check_24,
                        0
                    )
                } else {
                    txtTitle.setCompoundDrawables(null, null, null, null)
                }
                checkAction.invoke(valueToUse)
            }
        }
    }

}