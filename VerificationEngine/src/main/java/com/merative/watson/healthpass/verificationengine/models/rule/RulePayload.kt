package com.merative.watson.healthpass.verificationengine.models.rule

import com.google.gson.annotations.SerializedName
import java.util.*

data class RulePayload(
    @SerializedName("id")
    val id: String,
    @SerializedName("version")
    val version: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("unrestricted")
    val unrestricted: Boolean,
    @SerializedName("name")
    val name: String,
    @SerializedName("createdBy")
    val createdBy: String,
    @SerializedName("predicate")
    val predicate: String,
    @SerializedName("createdAt")
    val createdAt: Date,
    @SerializedName("updatedAt")
    val updatedAt: Date
)