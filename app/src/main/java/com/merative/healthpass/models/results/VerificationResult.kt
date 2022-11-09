package com.merative.healthpass.models.results

import androidx.annotation.StringRes
import com.merative.healthpass.R

sealed class VerificationResult(@StringRes val messageResId: Int)

class CredentialValid : VerificationResult(R.string.result_Verified)

class InvalidSignature : VerificationResult(R.string.result_InvalidSignature)

class UnknownIssuer : VerificationResult(R.string.result_UnknownIssuer)

class CredentialRulesInvalid : VerificationResult(R.string.result_InvalidRules)

class UnknownVerificationError : VerificationResult(R.string.result_UnknownError)

class CredentialSignatureFailed : VerificationResult(R.string.result_signature_failed)