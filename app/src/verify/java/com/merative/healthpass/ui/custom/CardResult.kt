package com.merative.healthpass.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.merative.healthpass.R
import com.merative.healthpass.databinding.CardResultBinding
import com.merative.healthpass.extensions.getDrawableCompat
import com.merative.healthpass.extensions.isNotNullOrEmpty

class CardResult : LinearLayout {

    private lateinit var binding: CardResultBinding

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
    //endregion

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        binding = CardResultBinding.inflate(LayoutInflater.from(context), this)
        background = context.getDrawableCompat(R.drawable.square_with_rounded_corner)
        orientation = HORIZONTAL

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs, R.styleable.CardResult,
                defStyleAttr, 0
            )

            val icon = a.getDrawable(R.styleable.CardResult_cr_icon)
            if (icon != null) {
                binding.activeResultIcon.setImageDrawable(icon)
            }

            val title = a.getText(R.styleable.CardResult_cr_title)
            if (title.isNotNullOrEmpty()) {
                binding.activeResult.text = title
            }
            val buttonTitle = a.getText(R.styleable.CardResult_cr_description)
            if (buttonTitle.isNotNullOrEmpty()) {
                binding.activeResultDescription.text = buttonTitle
            }
            a.recycle()
        }
    }

    fun setData(
        @DrawableRes icon: Int,
        @StringRes titleId: Int,
        @StringRes descriptionId: Int,
    ) {
        binding.activeResultIcon.setImageResource(icon)
        binding.activeResult.text = context.getText(titleId)
        binding.activeResultDescription.text = context.getText(descriptionId)
    }

}