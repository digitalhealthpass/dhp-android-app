package com.merative.healthpass.utils

import android.accounts.NetworkErrorException
import com.google.gson.JsonParseException
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.models.api.BaseResponse
import com.merative.watson.healthpass.verifiablecredential.extensions.stringfy
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

object RxHelper {

    fun unsubscribe(vararg disposables: Disposable?) {
        for (disposable in disposables) {
            if (disposable is CompositeDisposable && !disposable.isDisposed) {
                disposable.clear()
            } else if (disposable != null && !disposable.isDisposed) {
                disposable.dispose()
            }
        }
    }

    fun <T> handleErrorSingle(
        t: Throwable,
        defaultForParseException: T? = null
    ): Single<Response<T>> {
        return when {
            t is JsonParseException -> // Successful response but failed to parse the expected object from the response body
//                Single.error(defaultForParseException)
                errorSingle("Error in response", AppConstants.SERVER_ERROR_PARSING_ISSUE)
            t is ConnectException || t is SocketTimeoutException -> errorSingle(
                "Connection Timeout",
                AppConstants.SERVER_ERROR_TIME_OUT
            )
            t is NetworkErrorException || t is UnknownHostException -> errorSingle(
                "No network",
                AppConstants.SERVER_ERROR_NO_INTERNET
            )
            t is SSLHandshakeException -> errorSingle(
                "Couldn't reach the server",
                AppConstants.SERVER_ERROR_SSL
            )
            else -> Single.error(t)
        }
    }

    private fun <T> errorSingle(message: String, errorCode: Int): Single<Response<T>> =
        getMockApiSingleError(errorCode, BaseResponse().apply { this.message = message })

    private fun <T> getMockApiSingleError(
        mockResponseHttpCode: Int,
        baseResponse: BaseResponse
    ): Single<Response<T>> {
        val errorJSON = baseResponse.stringfy()
        val mockResponseBody = errorJSON.toResponseBody("application/json".toMediaTypeOrNull())
        val mockErrorResponse = Response.error<T>(mockResponseHttpCode, mockResponseBody)
        return Single.just(mockErrorResponse)
    }
}

fun <T> Flowable<T>.asyncToUiSingle(): Flowable<T> = compose {
    it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T> Single<T>.asyncToUiSingle(): Single<T> = compose {
    it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T> Maybe<T>.asyncToUiSingle(): Maybe<T> = compose {
    it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun Completable.asyncToUiCompletable(): Completable = compose {
    it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T> T.toSingle(): Single<T> = Single.just(this)

inline fun <T> Single<T>.ifThen(
    condition: Boolean, thenSource: () -> Single<T>,
    elseSource: () -> Single<T>
): Single<*> = if (condition) thenSource() else elseSource()

/**
 * Ensure the condition is true, else throw the provided @exception or [IllegalStateException]
 */
fun <T> Single<T>.ensure(
    exception: Exception? = null,
    condition: (value: T) -> Boolean
): Single<T> {
    return flatMap {
        if (condition.invoke(it)) {
            Single.just(it)
        } else {
            if (exception != null) {
                throw exception
            } else {
                throw IllegalStateException("Ensuring the condition has failed")
            }
        }
    }
}

/**
 *
 */
fun <T> T.toSingleAndEnsure(
    exception: Exception? = null,
    condition: (value: T) -> Boolean
): Single<T> {
    return toSingle().ensure(exception, condition)
}
