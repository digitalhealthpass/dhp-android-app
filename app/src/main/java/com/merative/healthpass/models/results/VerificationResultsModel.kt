package com.merative.healthpass.models.results

import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import com.merative.watson.healthpass.verificationengine.VerifyEngine

data class VerificationResultsModel(
    var isNotRevoked: Boolean = false,
    var isSignVerified: Boolean = false,
    var verifiableObject: VerifiableObject? = null,
    var verifyEngine: VerifyEngine? = null,
    var result: VerificationResult? = null,
)
