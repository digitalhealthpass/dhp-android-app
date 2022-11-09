package com.merative.healthpass.models.api.metadata

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MetaResponse(val payload: IssuerMetaData) : Parcelable

@Parcelize
data class IssuerMetaData(
    val id: String,
    val metadata: MetaData?,
    val type: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updated_at: String,
) : Parcelable

@Parcelize
data class MetaData(
    val name: String?,
    val logo: String?
) : Parcelable