package com.merative.healthpass.extensions

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.merative.healthpass.R
import com.merative.healthpass.databinding.SimpleCustomSnackbarLayoutBinding


fun View.snackShort(text: CharSequence, duration: Int = 3000) {

    //Create Snack
    val snackbar = Snackbar.make(this, "", duration)
    //Get & Set Snack attributes
    snackbar.view.setBackgroundColor(Color.TRANSPARENT)
    val snackLayout = snackbar.view as Snackbar.SnackbarLayout
    snackLayout.setPadding(0, 0, 0, 0)
    //Inflate and customize custom layout

    var binding = SimpleCustomSnackbarLayoutBinding.inflate(LayoutInflater.from(this.context))

    binding.tvMessage.text = text
    binding.btnAction.text = resources.getString(R.string.ok)
    binding.btnAction.setOnClickListener {
        snackbar.dismiss()
    }
    //Add custom layout to snack layout
    snackLayout.addView(binding.root, 0)
    snackbar.show()
}

inline var View.isEnabledAlpha: Boolean
    get() = isEnabled
    set(value) {
        isEnabled = value
        alpha = if (isEnabled) {
            1f
        } else {
            0.5f
        }
    }

fun ViewPager2.hasNext() = currentItem < adapter?.itemCount.orValue(0) - 1
fun ViewPager2.next() {
    currentItem += 1
}

fun ViewPager2.hasPrevious() = currentItem > 0
fun ViewPager2.previous() {
    currentItem += 1
}

//region keyboard
fun View.hideKeyboard() {
    val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    im.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

fun View.showKeyboard() {
    val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    im.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}
//endregion

fun TextView.setTypeFaceRes(@FontRes fontId: Int) {
    val typeface = ResourcesCompat.getFont(context, fontId)
    setTypeface(typeface)
}

fun View.margin(
    left: Float? = null,
    top: Float? = null,
    right: Float? = null,
    bottom: Float? = null
) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.run { leftMargin = dpToPx(this) }
        top?.run { topMargin = dpToPx(this) }
        right?.run { rightMargin = dpToPx(this) }
        bottom?.run { bottomMargin = dpToPx(this) }
    }
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
    else logw("can't adjust margin")
}

fun View.dpToPx(dp: Float): Int = context.dpToPx(dp)