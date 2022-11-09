package com.merative.healthpass.extensions

import com.google.gson.JsonSyntaxException
import com.merative.healthpass.models.api.BaseResponse
import com.merative.watson.healthpass.verifiablecredential.utils.GsonHelper
import retrofit2.HttpException

fun HttpException.getBaseResponse(): BaseResponse? {
    this.response()?.body()?.apply {
        return if (this is BaseResponse) this else null
    }
    this.response()?.errorBody()?.apply {
        val responseString = string()
        return try {
            GsonHelper.gson.fromJson(responseString, BaseResponse::class.java)
        } catch (ex: JsonSyntaxException) {
            loge("", ex, true)
            return null
        }
    }
    return null
}

//fun HttpException.getBaseResponseErrorCode(): String? = getBaseResponse()?.error

//fun HttpException.peekBaseResponse(): BaseResponse? {
//    this.response()?.body()?.apply {
//        return if (this is BaseResponse) this else null
//    }
//    this.response()?.errorBody()?.apply {
//        return Gson().fromJson(this.source().peek().readString(Util.bomAwareCharset(this.source().peek(),
//                this.contentType()?.charset(UTF_8) ?: UTF_8)),
//                BaseResponse::class.java)
//    }
//    return null
//}