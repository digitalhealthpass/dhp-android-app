package com.merative.watson.healthpass.verificationengine.models.rule

import java.util.*

data class RuleSetPayload(
    val id: String,
    val version: String,
    val unrestricted: Boolean,
    val name: String,
    val type: String,
    val createdBy: String,
    val rules: List<RuleMetadata>,
    val schemas: List<String>,
    val createdAt: Date,
    val updatedAt: Date
)