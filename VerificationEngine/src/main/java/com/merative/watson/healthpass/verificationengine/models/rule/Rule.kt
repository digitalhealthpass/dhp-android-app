package com.merative.watson.healthpass.verificationengine.models.rule

data class Rule(
    val type: String,
    val payload: List<RulePayload>
)