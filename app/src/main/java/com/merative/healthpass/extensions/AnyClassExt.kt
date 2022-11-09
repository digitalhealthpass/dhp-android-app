package com.merative.healthpass.extensions

import android.util.Base64
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.merative.healthpass.BuildConfig
import com.merative.watson.healthpass.verifiablecredential.extensions.javaName
import com.merative.watson.healthpass.verifiablecredential.extensions.stringfy
import io.reactivex.rxjava3.functions.Consumer
import java.util.*

fun <R : Any> R.logv(message: String) = Log.v(javaName(), message)
fun <R : Any> R.logd(message: String, throwable: Throwable? = null) =
    Log.d(">>> $${javaName()}", message, throwable)

fun <R : Any> R.logi(message: String) = Log.i(">>> $${javaName()}", message)
fun <R : Any> R.logw(message: String, throwable: Throwable? = null) =
    Log.w(javaName(), message, throwable)

fun <R : Any> R.loge(
    message: String,
    throwable: Throwable? = null,
    reportToFirebase: Boolean = false
) {
    Log.e(javaName(), message, throwable)

    if (throwable is Exception && reportToFirebase && BuildConfig.DEBUG) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }
}

fun <R : Any> R?.orValue(value: R): R {
    return this ?: value
}

fun <R : Any> R.rxError(message: String): Consumer<Throwable> = Consumer { throwable ->
    Log.e(javaName(), message, throwable)
}

fun Any.stringfyAndEncodeBase64(): String =
    Base64.encodeToString(
        this.stringfy().toByteArray(),
        Base64.DEFAULT or Base64.NO_WRAP
    ).trim()

/**
 * version name and version code
 */
inline val buildVersion: String
    get() {
        return "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
    }

inline val IS_QA: Boolean
    get() {
        return BuildConfig.BUILD_TYPE == "qa"
    }

fun <T> randomObject(vararg values: T): T {
    return values[Random().nextInt(values.size)]
}

fun <T> randomObject(values: List<T>): T {
    return if (values.isNullOrEmpty()) {
        throw IllegalStateException("list is null or empty")
    } else {
        values[Random().nextInt(values.size)]
    }
}