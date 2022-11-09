package com.merative.healthpass.models.api.schema

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.merative.healthpass.models.api.BaseResponse
import com.merative.watson.healthpass.verifiablecredential.models.credential.Proof
import com.merative.watson.healthpass.verifiablecredential.models.credential.Schema
import kotlinx.parcelize.Parcelize

@Parcelize
data class SchemaResponse(
    var type: String?,
    var payload: SchemaPayload?
) : BaseResponse(), Parcelable

@Parcelize
data class SchemaPayload(
    var id: String,
    @SerializedName("@type")
    var type: String,
    var modelVersion: String?,
    var name: String?,
    var author: String?,
    var authored: String?,
    var schema: Schema?,
    var proof: Proof?,
    var authorName: String?
) : Parcelable