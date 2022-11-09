package com.merative.watson.healthpass.verificationengine.models.rule

data class RuleSet(
    var type: String,
    var payload: List<RuleSetPayload>
)