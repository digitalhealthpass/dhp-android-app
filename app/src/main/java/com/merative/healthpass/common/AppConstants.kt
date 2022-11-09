package com.merative.healthpass.common

import androidx.core.content.ContextCompat
import com.merative.healthpass.R
import java.util.concurrent.TimeUnit

object AppConstants {
    const val INVALID_RESOURCE: Int = -1

    val ACCESS_TOKEN_VALID_TIME = TimeUnit.MINUTES.toMillis(30)

    const val COLOR_SHC_CREDENTIAL = "#0072C3"
    const val COLOR_DCC_CREDENTIAL = "#002d9c"
    var COLOR_CREDENTIALS = "#000000"
        private set

    const val INVALID_RES_ID = -1

    //local error codes
    const val ERROR_UNKNOWN = -1
    const val ERROR_ACCESS_TOKEN_NOT_FOUND = -3

    //server response code
    const val SERVER_ERROR_JSON_PARSE = 111
    const val SERVER_ERROR_BAD_REQUEST = 400
    const val SERVER_ERROR_UNAUTHORIZED_ACCOUNT = 401
    const val SERVER_ERROR_FAILED_TO_GET_SCHEMA = 404
    const val SERVER_ERROR_TIME_OUT = 408
    const val SERVER_ERROR_PARSING_ISSUE = 499
    const val SERVER_ERROR_CONNECT = 500
    const val SERVER_ERROR_WRONG_USER_PASSWORD = 501
    const val SERVER_ERROR_NO_INTERNET = 502
    const val SERVER_ERROR_UNKNOWN_HOST = 506
    const val SERVER_ERROR_SSL = 600

    const val FOLDER_TEMP = "temp"

    const val BIOMETRIC_PIN = "-1"

    //default shared pref
    const val KEY_PREF_ROOT_CHECK = "disable_root_check"

    fun adjustValues(app: App) {
        COLOR_CREDENTIALS =
            "#" + Integer.toHexString(ContextCompat.getColor(app, R.color.credentialColor))
    }
}