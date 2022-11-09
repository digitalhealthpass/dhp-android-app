package com.merative.healthpass.ui.common.custom

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.widget.LinearLayout
import com.merative.healthpass.R
import com.merative.healthpass.databinding.BtnTimerBinding
import com.merative.healthpass.extensions.isNotNullOrEmpty

class ButtonWithTimer : LinearLayout {

    lateinit var countDownTimer: CountDownTimer
        private set

    private lateinit var binding: BtnTimerBinding

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
        binding = BtnTimerBinding.inflate(LayoutInflater.from(context), this, true)

        setPadding(0, context.resources.getDimensionPixelSize(R.dimen.margin_8), 0, 0)

        binding.timer.setTextColor(context.getColor(R.color.btn_blue_enabled))

        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs, R.styleable.ButtonWithTimer,
                defStyleAttr, 0
            )

            val title = a.getText(R.styleable.ButtonWithTimer_bwt_text)
            if (title.isNotNullOrEmpty()) {
                binding.text.text = title
            }
            val buttonTitle = a.getText(R.styleable.ButtonWithTimer_bwt_btn_text)
            if (buttonTitle.isNotNullOrEmpty()) {
                binding.timer.text = buttonTitle
            }
            val timerMax = a.getInt(R.styleable.ButtonWithTimer_bwt_timer_max, 30000)

            countDownTimer = object : CountDownTimer(timerMax.toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.timer.isEnabled = false
                    binding.timer.setTextColor(context.getColor(R.color.subTextLabels))
                    binding.timer.text = "Resend in: " + millisUntilFinished / 1000
                }

                override fun onFinish() {
                    binding.timer.isEnabled = true
                    binding.timer.setTextColor(context.getColor(R.color.btn_blue_enabled))
                    binding.timer.text = buttonTitle
                }

            }
            a.recycle()
        }
        countDownTimer.start()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        val click = OnClickListener {
            countDownTimer.start()
            l?.onClick(binding.timer)
        }
        binding.timer.setOnClickListener(click)
    }

    fun reset() {
        countDownTimer.cancel()
        countDownTimer.onFinish()
    }
}