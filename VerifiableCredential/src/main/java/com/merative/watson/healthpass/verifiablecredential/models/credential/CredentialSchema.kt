package com.merative.watson.healthpass.verifiablecredential.models.credential

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CredentialSchema(
    @SerializedName("id")
    var id: String?,
    @SerializedName("type")
    var type: String?
) : Parcelable
