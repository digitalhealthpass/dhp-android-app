package com.merative.healthpass.ui.common.recyclerView

import android.content.Context
import android.content.res.Resources
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.merative.healthpass.R

/**
 * A concrete class of an empty ViewHolder.
 * Should be used to avoid the boilerplate of creating a ViewHolder class for simple case
 * scenarios.
 */
class SimpleViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
    var model: T? = null
    var textListener: TextWatcher? = null

    val context: Context get() = itemView.context
    val resources: Resources get() = context.resources

    fun removeTextWatcher() {
        itemView.findViewById<TextInputLayout>(R.id.til)
            ?.editText?.removeTextChangedListener(textListener)
        textListener = null
    }
}