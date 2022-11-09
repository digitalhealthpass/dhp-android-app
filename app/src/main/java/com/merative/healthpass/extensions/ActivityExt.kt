package com.merative.healthpass.extensions

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.merative.healthpass.BuildConfig
import com.merative.healthpass.R
import com.merative.healthpass.databinding.DialogEditTextBinding
import com.merative.healthpass.databinding.DialogTermsBinding
import com.merative.healthpass.ui.debug.DebugActivity
import com.merative.healthpass.utils.Utils

/**
 * this will open the app again, and close all opened activities
 */
fun Activity.restartApplication() {
    val packageManager: PackageManager = packageManager
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    val componentName = intent?.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}

fun FragmentActivity.showDebugDialog() {
    if (BuildConfig.DEBUG) {
        val positiveListener = { _: DialogInterface ->
            val intent = Intent(this, DebugActivity::class.java)
            this.startActivity(intent)
        }
        showDialog(
            null,
            getString(R.string.dialog_debug_message),
            getString(R.string.button_title_ok), positiveListener,
            getString(R.string.button_title_cancel), { }
        )
    }
}

fun FragmentActivity.showDialog(
    title: CharSequence?,
    message: CharSequence,
    positiveText: CharSequence, positiveClick: (dialog: DialogInterface) -> Unit,
    negativeText: CharSequence, negativeClick: () -> Unit,
    icon: Int? = null
) {
    val binding = DialogTermsBinding.inflate(LayoutInflater.from(this), null, false)
    binding.positiveTextView.text = positiveText
    binding.negativeTextView.text = negativeText
    binding.messageTextView.text = message
    binding.titleTextview.text = title

    binding.negativeTextView.visibility = if (negativeText.isEmpty()) View.GONE else View.VISIBLE
    binding.positiveTextView.visibility = if (positiveText.isEmpty()) View.GONE else View.VISIBLE
    binding.titleTextview.visibility = if (Utils.StringUtils.isAllValid(title as String?)) View.VISIBLE else View.GONE
    binding.messageTextView.visibility = if (Utils.StringUtils.isAllValid(message as String?)) View.VISIBLE else View.GONE

    val alertDialog = MaterialAlertDialogBuilder(this)
    alertDialog.setView(binding.root)

    val alert = alertDialog.create()
    alert.setCanceledOnTouchOutside(false)
    alert.show()

    if (icon != null) {
        alertDialog.setIcon(icon)
    }

    binding.positiveTextView.setOnClickListener {
        positiveClick.invoke(alert)
        alert.dismiss()
    }
    binding.negativeTextView.setOnClickListener {
        negativeClick.invoke()
        alert.dismiss()
    }
}

fun FragmentActivity.showTermsDialog(
    title: CharSequence?,
    message: CharSequence,
    positiveText: CharSequence, positiveClick: (dialog: DialogInterface) -> Unit,
    negativeText: CharSequence, negativeClick: () -> Unit
) {
    val binding = DialogTermsBinding.inflate(LayoutInflater.from(this), null, false)

    binding.positiveTextView.text = positiveText
    binding.negativeTextView.text = negativeText
    binding.messageTextView.text = message
    binding.titleTextview.text = title

    binding.negativeTextView.visibility = if (negativeText.isEmpty()) View.GONE else View.VISIBLE
    binding.positiveTextView.visibility = if (positiveText.isEmpty()) View.GONE else View.VISIBLE
    binding.titleTextview.visibility = if (Utils.StringUtils.isAllValid(title as String?)) View.VISIBLE else View.GONE
    binding.messageTextView.visibility = if (Utils.StringUtils.isAllValid(message as String?)) View.VISIBLE else View.GONE

    val alertDialog = MaterialAlertDialogBuilder(this)
    alertDialog.setView(binding.root)

    val alert = alertDialog.create()
    alert.setCanceledOnTouchOutside(false)
    alert.show()

    binding.positiveTextView.setOnClickListener { alert.dismiss() }
    binding.negativeTextView.setOnClickListener {
        negativeClick.invoke()
        alert.dismiss()
    }
}

/**
 * Show a dialog that will have only positive button and a default action which is dismissing the dialog
 */
fun FragmentActivity.showDialog(
    title: CharSequence?,
    message: CharSequence,
    positiveText: CharSequence, positiveClick: (dialog: DialogInterface) -> Unit
) {
    showDialog(
        title,
        message,
        positiveText,
        positiveClick,
        "", {}
    )
}

fun FragmentActivity.showInputDialog(
    title: CharSequence?,
    editTextHint: CharSequence,
    positiveText: CharSequence, positiveClick: (dialog: DialogInterface, text: String) -> Unit,
    negativeText: CharSequence, negativeClick: () -> Unit,
    dismissListener: () -> Unit = {},
    showPassword: Boolean = false
) {
    val binding = DialogEditTextBinding.inflate(LayoutInflater.from(this), null, false)

    binding.til.hint = editTextHint
    binding.til.contentDescription = editTextHint

    binding.positiveTextView.text = positiveText
    binding.negativeTextView.text = negativeText
    binding.titleTextview.text = title

    binding.negativeTextView.visibility = if (negativeText.isEmpty()) View.GONE else View.VISIBLE
    binding.positiveTextView.visibility = if (positiveText.isEmpty()) View.GONE else View.VISIBLE
    binding.titleTextview.visibility = if (Utils.StringUtils.isAllValid(title as String?)) View.VISIBLE else View.GONE

    if (showPassword) {
        binding.til.editText?.inputType = InputType.TYPE_CLASS_TEXT
    }

    binding.til.editText?.minimumHeight = resources.getDimension(R.dimen.minAccessibilityHeight).toInt()

    val alertDialog = MaterialAlertDialogBuilder(this)
    alertDialog.setView(binding.root)

    val alert = alertDialog.create()
    alert.setCanceledOnTouchOutside(false)
    alert.setOnDismissListener { dismissListener.invoke() }
    alert.show()

    binding.positiveTextView.setOnClickListener {
        positiveClick.invoke(alert, binding.til.editText?.text?.toString().orEmpty())
        alert.dismiss()
    }
    binding.negativeTextView.setOnClickListener {
        negativeClick.invoke()
        alert.dismiss()
    }
}

fun Activity.sendEmail(email: String, subject: CharSequence, body: CharSequence) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
        putExtra(Intent.EXTRA_HTML_TEXT, body)
    }
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    } else {
        window.decorView.rootView.snackShort(getString(R.string.no_app_for_email))
    }
}

fun Activity.openDialer(tel: String) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$tel")
    startActivity(intent)
}

fun Activity.goToSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri: Uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    startActivity(intent)
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}