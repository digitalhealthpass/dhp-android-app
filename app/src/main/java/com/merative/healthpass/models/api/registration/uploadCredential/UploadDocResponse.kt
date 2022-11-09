package com.merative.healthpass.models.api.registration.uploadCredential

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.merative.healthpass.models.api.BaseResponse
import kotlinx.parcelize.Parcelize

class UploadDocResponse(val payload: UploadDocPayLoad?) : BaseResponse(), Parcelable

@Parcelize
data class UploadDocPayLoad(
    val id: String,
    val link: String,
    val type: String,
    val name: String,
    val content: String,
    val accessed: Boolean,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("expires_at")
    val expiresAt: String
) : Parcelable