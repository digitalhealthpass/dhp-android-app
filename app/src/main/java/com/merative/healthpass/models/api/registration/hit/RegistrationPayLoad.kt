package com.merative.healthpass.models.api.registration.hit

import android.os.Parcelable
import com.google.gson.JsonObject
import com.merative.watson.healthpass.verifiablecredential.models.parcel.JsonElementParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import kotlinx.parcelize.WriteWith

@Parcelize
@TypeParceler<JsonObject, JsonElementParceler>
data class RegistrationPayLoad(
    val type: String,
    val org: String,
    val entityType: String?,
    val flow: RegFlow,
    val userAgreement: String,
    val registrationForm: @WriteWith<JsonElementParceler> JsonObject?
) : Parcelable {
    companion object {
        const val HOLDER_UPLOAD = "holder-upload"
        const val HOLDER_DOWNLOAD = "holder-download"
    }
}

@Parcelize
data class RegFlow(
    val registrationCodeAuth: Boolean,
    val mfaAuth: Boolean,
    val showUserAgreement: Boolean,
    val showRegistrationForm: Boolean
) : Parcelable