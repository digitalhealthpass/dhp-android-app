package com.merative.watson.healthpass.extension

import android.content.DialogInterface
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun FragmentActivity.showDialog(
    title: CharSequence?,
    message: CharSequence,
    positiveText: CharSequence, positiveClick: (dialog: DialogInterface) -> Unit,
    negativeText: CharSequence, negativeClick: () -> Unit,
) {
    val alertDialog = MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positiveText) { dialog, _ -> positiveClick.invoke(dialog) }

    if (negativeText.isNotEmpty()) {
        alertDialog.setNegativeButton(negativeText) { _, _ -> negativeClick.invoke() }
    }

    val alert = alertDialog.create()
    alert.setCanceledOnTouchOutside(false)
    alert.show()
}