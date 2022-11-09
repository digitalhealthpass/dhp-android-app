package com.merative.healthpass.models.api

import android.os.Parcelable
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.merative.healthpass.extensions.isNotNullOrEmpty
import com.merative.healthpass.extensions.removeExtraQuote
import com.merative.watson.healthpass.verifiablecredential.extensions.getStringOrNull
import com.merative.watson.healthpass.verifiablecredential.models.parcel.JsonElementParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.parcelize.WriteWith

@Parcelize
@TypeParceler<JsonObject, JsonElementParceler>
open class BaseResponse(
    var message: String? = null,
    var error: @WriteWith<JsonElementParceler> JsonElement? = null
) : Parcelable {

    val errorOrMessage: String?
        get() {
            return when {
                error is JsonPrimitive && error?.asString.isNotNullOrEmpty() -> {
                    error.toString().removeExtraQuote()
                }
                error is JsonObject && (error as JsonObject).getStringOrNull("message")
                    .isNotNullOrEmpty() -> {
                    (error as JsonObject).getStringOrNull("message")
                }
                else -> {
                    message
                }
            }
        }
}