package com.merative.healthpass.extensions

import android.accounts.NetworkErrorException
import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import com.merative.healthpass.BuildConfig
import com.merative.healthpass.R
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.models.api.ApiDataException
import com.merative.healthpass.ui.mainActivity.MainActivity
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.net.ssl.SSLHandshakeException

fun Fragment.showFileImport(requestCode: Int) {
    val mimeTypes = arrayOf(
        "application/zip",
        "application/octet-stream",
        "application/x-zip-compressed",
        "multipart/x-zip"
    )
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        .addCategory(Intent.CATEGORY_OPENABLE)
        .setType("*/*")
        .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivityForResult(intent, requestCode)
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Fragment.biometricAuthenticate(
    success: () -> Unit,
    error: () -> Unit,
    failed: () -> Unit
) {
    val executor = ContextCompat.getMainExecutor(requireActivity())
    val biometricManager = BiometricManager.from(requireActivity())

    if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.bio_auth_title))
            .setSubtitle(getString(R.string.bio_auth_message))
            .setDeviceCredentialAllowed(true)
            .build()

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    success.invoke()
                }

                override fun onAuthenticationError(
                    errorCode: Int, errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    error.invoke()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    failed.invoke()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }
}

fun Fragment.getStringForList(
    list: List<*>,
    @StringRes singularResourceId: Int,
    @StringRes pluralResourceId: Int
): String {
    return if (list.size == 1) getString(singularResourceId, list.size)
    else getString(pluralResourceId, list.size)
}

fun Fragment.shareFile(filePath: String, requestCode: Int, fileType: String) {
    shareFile(File(filePath), requestCode, fileType)
}

fun Fragment.shareFile(file: File, requestCode: Int, fileType: String) {
    val authority = BuildConfig.APPLICATION_ID

    val intent = Intent()
    intent.action = Intent.ACTION_SEND
    intent.putExtra(
        Intent.EXTRA_STREAM,
        FileProvider.getUriForFile(requireActivity(), authority, file)
    )
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    intent.type = fileType

    if (intent.resolveActivity(requireActivity().packageManager) != null) {
        startActivityForResult(intent, requestCode)
    } else {
        view?.snackShort("failed to send")
    }
}

//region error handling
fun Fragment.handleCommonErrors(response: Response<*>?) {
    val domain = getServerErrorDomain(response)
    val code = response?.code()
    val message = getServerErrorMessage(response)
    view?.snackShort(
        "$message \n(Domain=$domain | Code=$code)"
    )
}

fun Fragment.handleCommonErrors(throwable: Throwable) {
    when {
        throwable is HttpException -> {
            handleCommonErrors(throwable.response())
        }
        handleEncryptionExceptions(throwable) -> {
            //handled encryption exceptions
        }
        throwable is JsonParseException || throwable is MalformedJsonException -> {
            // Successful response but failed to parse the expected object from the response body
            view?.snackShort(getString(R.string.error_in_response) + " \n(Domain=JsonParse | Code=" + AppConstants.SERVER_ERROR_JSON_PARSE)
        }
        throwable is ConnectException || throwable is SocketTimeoutException -> {
            view?.snackShort(getString(R.string.connection_timedout) + " \n(Domain=Connect | Code=" + AppConstants.SERVER_ERROR_CONNECT)
        }
        throwable is NetworkErrorException || throwable is UnknownHostException -> {
            view?.snackShort(getString(R.string.no_internet) + " \n(Domain=UnknownHost | Code=" + AppConstants.SERVER_ERROR_UNKNOWN_HOST)
        }
        throwable is SSLHandshakeException -> {
            view?.snackShort(getString(R.string.couldnt_reach_server) + " \n(Domain=SSLHandShake | Code=" + AppConstants.SERVER_ERROR_SSL)
        }
        throwable is ApiDataException -> {
            loge(getString(R.string.api_missing_request_info), throwable)
            activity?.showDialog(
                null,
                getString(R.string.api_missing_request_info),
                getString(R.string.button_title_ok)
            ) {}
        }
        else -> {
            loge("error in request", throwable)
            activity?.showDialog(
                getString(R.string.error),
                getString(R.string.error_unknown),
                getString(R.string.button_title_ok)
            ) { }
        }
    }
}

fun Fragment.getServerErrorMessage(response: Response<*>?): String {
    val url = response?.raw()?.networkResponse?.request?.url.toString()
    val code = response?.code()
    return if (code == AppConstants.SERVER_ERROR_UNAUTHORIZED_ACCOUNT ||
        code == AppConstants.SERVER_ERROR_WRONG_USER_PASSWORD ||
        code == AppConstants.SERVER_ERROR_TIME_OUT ||
        code == AppConstants.SERVER_ERROR_NO_INTERNET ||
        code == AppConstants.SERVER_ERROR_PARSING_ISSUE
    ) {
        getString(R.string.try_again)
    } else if (code == AppConstants.SERVER_ERROR_FAILED_TO_GET_SCHEMA) {
        getString(R.string.scan_verification_errorMessage)
    } else if (code == AppConstants.SERVER_ERROR_BAD_REQUEST) {
        if (url.contains("organization")) {
            getString(R.string.reg_failed_org_message)
        } else if (url.contains("validatecode") && url.contains("datasubmission")) {
            getString(R.string.reg_failed_rcode_message)
        } else if (url.contains("datasubmission")) {
            getString(R.string.reg_failed_vcode_message)
        } else {
            getString(R.string.try_again)
        }
    } else {
        response?.errorBaseResponse()?.errorOrMessage.orValue(getString(R.string.error_unknown))
    }
}

fun getServerErrorDomain(response: Response<*>?): String {
    val url = response?.raw()?.networkResponse?.request?.url.toString()
    return when {
        url.contains("schemas") -> "Schema"
        url.contains("issuers") -> "Issuers"
        url.contains("datasubmission") -> "Data Submission"
        else -> "Unknown"
    }
}

fun Fragment.handleEncryptionExceptions(throwable: Throwable): Boolean {
    return if (
        throwable is NoSuchPaddingException
        || throwable is NoSuchAlgorithmException
        || throwable is InvalidAlgorithmParameterException
        || throwable is InvalidKeyException
        || throwable is BadPaddingException
        || throwable is IllegalBlockSizeException
    ) {
        view?.snackShort(getString(R.string.encryption_errors))
        true
    } else {
        false
    }
}
//endregion

//region support action bar / app bar layout
fun Fragment.applySupportActionBar(block: (ActionBar).() -> Unit) {
    (activity as AppCompatActivity?)?.supportActionBar?.apply(block)
}

fun Fragment.applyAppBarLayout(block: (AppBarLayout).() -> Unit) {
    (activity as MainActivity?)?.appBarLayout?.apply(block)
}

/**
 * use this function to show and hide [AppBarLayout], ultimately we changing the height of toolbar,
 * since the coordinator is not always redrawn correctly
 */
fun Fragment.showActionBar(show: Boolean = true) {
    applyAppBarLayout {
        (layoutParams as? CoordinatorLayout.LayoutParams)?.height = if (show)
            resources.getDimensionPixelSize(R.dimen.toolbar_height)
        else
            0
    }
}

fun Fragment.lockAppBar(scrollView: View?, canDrag: Boolean) {
    if (scrollView == null) return

    applyAppBarLayout {
        /* Disable the nestedScrolling to disable expanding the
            appBar with dragging the nestedScrollView below it */
        ViewCompat.setNestedScrollingEnabled(scrollView, canDrag)

        /* But still appBar is expandable with dragging the appBar itself
             and below code disables that too
        */
        val behavior =
            (layoutParams as? CoordinatorLayout.LayoutParams)?.behavior as? AppBarLayout.Behavior
        behavior!!.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return canDrag
            }
        })
    }
}

/** Methods to change behavior of scroll behavior of Collapsing Toolbar*/
fun Fragment.removeScrollFlags() {
    applyScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL)
}

fun Fragment.applyDefaultScrollFlags() {
    val defaultScrollFlags: Int = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
            AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or
            AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
    applyScrollFlags(defaultScrollFlags)
}

fun Fragment.applyScrollFlags(scrollFlags: Int) {
    val layout = (activity as MainActivity?)?.binding?.collapsingToolbarLayout
    val params = layout?.layoutParams as? AppBarLayout.LayoutParams

    params?.scrollFlags = scrollFlags
}
//endregion