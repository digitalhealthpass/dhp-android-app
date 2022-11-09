package com.merative.healthpass.models.api.registration.nih

import android.os.Parcelable
import com.google.gson.JsonObject
import com.merative.healthpass.models.api.BaseResponse
import com.merative.watson.healthpass.verifiablecredential.models.parcel.JsonElementParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

@Parcelize
@TypeParceler<JsonObject, JsonElementParceler>
data class DisplaySchemaResponse(
    val payload: String
) : BaseResponse(), Parcelable
