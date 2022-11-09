package com.merative.watson.healthpass.verificationengine.exception

sealed class VerifyError(message: String) : Exception(message) {
    object CredentialSignatureNoProperties :
        VerifyError("Some properties were missing, and could not verify")

    object CredentialSignatureNoPublicKey :
        VerifyError("Public key was missing, and could not verify")

    object CredentialSignatureFailed : VerifyError("Signature failed")
    object CredentialSignatureUnsupportedKey : VerifyError("the key is unsupported")
    object CredentialSignatureInvalidCredentialData : VerifyError("the credential signature is invalid")
    object CredentialSignatureInvalidSignatureData : VerifyError("the signature is invalid")
}