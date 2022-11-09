package com.merative.healthpass.ui.common.baseViews

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.merative.healthpass.R
import com.merative.healthpass.extensions.logw

class LoadingDialog(private var activity: Activity?) : LifecycleObserver {

    var dialog: Dialog? = null

    fun showLoading(showLoading: Boolean) {
        if (!showLoading) {
            if (activity != null && dialog != null && dialog?.isShowing == true) {
                try {
                    dialog?.dismiss()
                } catch (ex: IllegalArgumentException) {
                    ex.printStackTrace()
                    dialog = null
                } catch (ex: Exception) {
                    ex.printStackTrace()
                } finally {
                    dialog = null
                }
            }
        } else {
            if (activity == null) {
                logw("dialog is null")
            } else if (activity != null) {
                val llPadding = 30
                val ll = LinearLayout(activity)
                ll.contentDescription = ""
                ll.orientation = LinearLayout.HORIZONTAL
                ll.setPadding(llPadding, llPadding, llPadding, llPadding)
                ll.gravity = Gravity.CENTER
                var llParam = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                llParam.gravity = Gravity.CENTER
                ll.layoutParams = llParam

                val progressBar = ProgressBar(activity)
                progressBar.isIndeterminate = true
                progressBar.setPadding(0, 0, llPadding, 0)
                progressBar.layoutParams = llParam
                progressBar.contentDescription = activity!!.getString(R.string.result_loading)

                llParam = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                llParam.gravity = Gravity.CENTER

                ll.addView(progressBar)

                val builder = MaterialAlertDialogBuilder(activity!!)
                    .setBackground(
                        activity!!.getColor(android.R.color.transparent).toDrawable()
                    )
                    .setView(ll)

                dialog = builder.create()
                dialog?.setCanceledOnTouchOutside(false)
                dialog?.setCancelable(false)
                dialog?.show()

                val window: Window? = dialog?.window
                if (window != null) {
                    val layoutParams = WindowManager.LayoutParams()
                    layoutParams.copyFrom(window.attributes)
                    layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                    layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                    window.attributes = layoutParams
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        activity = null
    }
}