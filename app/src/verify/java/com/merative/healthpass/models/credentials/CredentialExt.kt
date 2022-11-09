package com.merative.healthpass.models.credentials

import com.google.gson.JsonObject
import com.merative.watson.healthpass.verifiablecredential.extensions.getJsonObjectOrNull
import com.merative.watson.healthpass.verifiablecredential.extensions.toJsonElement
import com.merative.watson.healthpass.verifiablecredential.models.credential.Credential
import com.merative.watson.healthpass.verifiablecredential.models.credential.credentialSubjectType

/**
 * Determines if the verifier app can use this credential as the organization/verifier credential
 */
val Credential.isOrganizationCredential: Boolean
    get() {
        return credentialSubjectType == "VerifierCredential"
    }

val Credential.unsignedCredentialDictionary: JsonObject?
    get() {
        //Remove signatureValue for verification process
        return toJsonElement().asJsonObject.getJsonObjectOrNull("proof")
            ?.apply {
                remove("signatureValue")
                remove("obfuscation")
            }
    }