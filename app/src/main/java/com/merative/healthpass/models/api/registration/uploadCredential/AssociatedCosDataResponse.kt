package com.merative.healthpass.models.api.registration.uploadCredential

import android.os.Parcelable
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.merative.healthpass.models.api.BaseResponse
import com.merative.watson.healthpass.verifiablecredential.models.parcel.JsonElementParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.parcelize.WriteWith

open class AssociatedDataResponse() : BaseResponse()

@Parcelize
@TypeParceler<JsonObject, JsonElementParceler>
class AssociatedCosDataResponse(
    val payload: @WriteWith<JsonElementParceler> JsonElement?
) : AssociatedDataResponse(), Parcelable

@Parcelize
@TypeParceler<JsonObject, JsonElementParceler>
class AssociatedPostBoxDataResponse(
    val payload: @WriteWith<JsonElementParceler> JsonElement?
) : AssociatedDataResponse(), Parcelable