package com.merative.watson.healthpass.verifiablecredential.models.veriableObject

import android.util.Log
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.credential.Credential

fun VerifiableObject.isValidCredential(message: String): Boolean {
    return try {
        var credential: Credential = parse(message)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


fun VerifiableObject.getCredential(message: String): Credential? {

    val credential: Credential = parse(message)
    // Sanity check
    return if (credential.proof == null || credential.credentialSubject == null || !credential.type.contains(
            "VerifiableCredential"
        )
    ) {
        Log.d(VerifiableObject.TAG, "Credential - Proof, Type or Subject missing or Type invalid ")
        null
    } else {
        credential
    }
}

fun VerifiableObject.getCredentialType(credential: Credential?): VCType {
    if (credential?.type == null) {
        return VCType.UNKNOWN
    }
    return when {
        credential.type.contains(VCType.IDHP.value) -> {
            VCType.IDHP
        }
        credential.type.contains(VCType.GHP.value) -> {
            VCType.GHP
        }
        credential.type.contains(VCType.DIVOC.value) -> {
            VCType.DIVOC
        }
        credential.type.contains(VCType.VC.value) -> {
            VCType.VC
        }
        //TODO: Change it to unknown here
        else -> VCType.IDHP
    }

}