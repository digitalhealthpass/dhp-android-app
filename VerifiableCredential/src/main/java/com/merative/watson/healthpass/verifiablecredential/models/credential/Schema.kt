package com.merative.watson.healthpass.verifiablecredential.models.credential

import android.os.Parcelable
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.merative.watson.healthpass.verifiablecredential.models.parcel.JsonElementParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.parcelize.WriteWith

@Parcelize
@TypeParceler<JsonObject, JsonElementParceler>
data class Schema(
    @SerializedName("\$schema")
    var schema: String,
    @SerializedName("additionalProperties")
    var additionalProperties: Boolean?,
    @SerializedName("description")
    var description: String?,
    @SerializedName("properties")
    var properties: @WriteWith<JsonElementParceler> JsonObject?,
    @SerializedName("required")
    var required: List<String>?,
    @SerializedName("type")
    var type: String?
) : Parcelable
