package com.merative.healthpass.extensions

import android.app.Activity
import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.merative.healthpass.R
import java.io.File
import java.io.IOException

fun Context.shareFile(filePath: String) {
    val sendIntent = Intent()
    val file = File(filePath)
    val filePathString = "file://" + file.absolutePath
    sendIntent.action = Intent.ACTION_SEND
    sendIntent.putExtra(
        Intent.EXTRA_STREAM,
        FileProvider.getUriForFile(this, "com.codepath.fileprovider.healthpass", file)
    )
    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    sendIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    sendIntent.type = "application/zip"
    ContextCompat.startActivity(this, sendIntent, null)
}

const val FILE_TYPE_ZIP = "application/zip"
const val FILE_TYPE_JSON = "*/*"

fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (connectivityManager != null) {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> return true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return true
            }
        }
    }
    return false
}

fun Activity.snackShort(parentView: View, text: CharSequence, duration: Int = 3000) {
    Snackbar
        .make(parentView, text, duration)
        .setAction(getString(R.string.button_title_ok), {})
        .show()
}

val Context.defaultSharedPre: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

@Throws(IOException::class)
fun Context.loadAssetsFileAsString(path: String) = assets.open(path).contentToString()

fun Context.openUrl(url: String) {
    val finalURL: String = if (!url.startsWith("http://") && !url.startsWith("https://"))
        "http://$url"
    else
        url
    if (url.isNotEmpty()) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(finalURL))
        ContextCompat.startActivity(this, browserIntent, null)
    }
}

fun Context.copyToClipboard(label: String, text: CharSequence) {
    val clipBoard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipBoard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(this, getString(R.string.kpm_label), Toast.LENGTH_SHORT).show()
}

fun Context.getDrawableCompat(@DrawableRes resId: Int) = ContextCompat.getDrawable(this, resId)
fun Context.getColorCompat(@ColorRes resId: Int) = ContextCompat.getColor(this, resId)

fun Context.dpToPx(dp: Float): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    dp,
    resources.displayMetrics
).toInt()

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}