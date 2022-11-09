package com.merative.healthpass.extensions

import com.google.gson.JsonSyntaxException
import com.merative.healthpass.models.api.BaseResponse
import com.merative.watson.healthpass.verifiablecredential.utils.GsonHelper
import retrofit2.Response

/**
 * Be careful using this function, because once it access the error body string, calling it again
 * will not return any value, since the string will be null.
 *
 * Consider checking error code before using this
 */
fun <T> Response<T>.errorBaseResponse(): BaseResponse? {
    return if (errorBody() != null) {
        val responseString = errorBody()?.string().orEmpty()
        return try {
            if (responseString == "Unauthorized") {
                BaseResponse().apply { message = responseString }
            } else {
                GsonHelper.gson.fromJson(responseString, BaseResponse::class.java)
            }
        } catch (ex: JsonSyntaxException) {
            loge("failed to find error response", ex)
            null
        }
    } else {
        null
    }
}

fun <T> Response<T>.isSuccessfulAndHasBody() = isSuccessful && body() != null