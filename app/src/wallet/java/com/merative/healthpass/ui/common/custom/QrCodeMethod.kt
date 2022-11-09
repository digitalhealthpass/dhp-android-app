package com.merative.healthpass.ui.common.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.merative.healthpass.R
import com.merative.healthpass.databinding.QrCardMethodBinding

class QrCodeMethod : ConstraintLayout {

    private lateinit var binding: QrCardMethodBinding

    //region constructor
    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = QrCardMethodBinding.inflate(LayoutInflater.from(context), this)

        setPadding(0, context.resources.getDimensionPixelSize(R.dimen.margin_10), 0, 0)
        setBackgroundColor(context.getColor(R.color.cardBackground))

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.QrCodeMethod,
                defStyleAttr,
                0
            )

            val title = a.getText(R.styleable.QrCodeMethod_qcm_right_text)
            if (!title.isNullOrEmpty()) {
                binding.txtRight.text = title
            }

            val icon = a.getDrawable(R.styleable.QrCodeMethod_qcm_icon)
            if (icon != null) {
                binding.imgBtn.setImageDrawable(icon)
            }

            binding.imgBtn.post {
                binding.imgBtn.minimumHeight = resources.getDimension(R.dimen.minAccessibilityHeight).toInt()
            }

            val showDivider = a.getBoolean(R.styleable.QrCodeMethod_qcm_show_divider, true)
            binding.divider.isVisible = showDivider

            a.recycle()
        }
    }
    //endregion

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener(l)
        binding.imgBtn.setOnClickListener(l)
    }
}