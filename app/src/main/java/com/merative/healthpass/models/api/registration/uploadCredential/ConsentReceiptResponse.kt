package com.merative.healthpass.models.api.registration.uploadCredential

import android.os.Parcelable
import com.google.gson.JsonObject
import com.merative.healthpass.models.api.BaseResponse
import com.merative.watson.healthpass.verifiablecredential.models.parcel.JsonElementParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.parcelize.WriteWith

@Parcelize
@TypeParceler<JsonObject, JsonElementParceler>
data class ConsentReceiptResponse(
    val payload: @WriteWith<JsonElementParceler> JsonObject
) : BaseResponse(), Parcelable