package com.merative.healthpass.ui.common.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.merative.healthpass.R
import com.merative.healthpass.databinding.LabelValueItemBinding

class LabelValueItem : LinearLayout {

    lateinit var binding: LabelValueItemBinding
        private set

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

        binding = LabelValueItemBinding.inflate(LayoutInflater.from(context), this)

        orientation = VERTICAL

        val minH = context.resources.getDimensionPixelSize(R.dimen.minAccessibilityHeight)
        minimumHeight = minH

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.LabelValueItem, 0, 0)


            val left = a.getText(R.styleable.LabelValueItem_lvi_left)
            if (!left.isNullOrEmpty()) {
                binding.left.text = left
            }
            val leftColor = a.getInt(R.styleable.LabelValueItem_lvi_left_color, R.color.textLabels)
            binding.left.setTextColor(context.getColor(leftColor))

            val right = a.getText(R.styleable.LabelValueItem_lvi_right)
            if (!right.isNullOrEmpty()) {
                binding.right.text = right
            }

            val rightColor =
                a.getInt(R.styleable.LabelValueItem_lvi_right_color, R.color.subTextLabels)
            binding.right.setTextColor(context.getColor(rightColor))

            val showDivider = a.getBoolean(R.styleable.LabelValueItem_lvi_show_divider, true)

            if (showDivider) {
                binding.divider.visibility = View.VISIBLE
            } else {
                binding.divider.visibility = View.GONE
            }
        }
    }
    //endregion
}