package com.merative.watson.healthpass.verifiablecredential.models.cwt

import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.merative.watson.healthpass.verifiablecredential.extensions.getInt64orDoubleAsInt64
import com.merative.watson.healthpass.verifiablecredential.extensions.getOrNull
import com.upokecenter.cbor.CBORObject

data class CWT(
    val iss: String?,
    val exp: Long?,
    val iat: Long?,
    val nbf: Long?,
    val sub: String?,
    val certificate: Certificate?
) {
    constructor(cbor: CBORObject): this(
        cbor.getOrNull(INDEX_ISS)?.AsString(),
        cbor.getOrNull(INDEX_EXP)?.getInt64orDoubleAsInt64(),
        cbor.getOrNull(INDEX_IAT)?.getInt64orDoubleAsInt64(),
        cbor.getOrNull(INDEX_NBF)?.getInt64orDoubleAsInt64(),
        cbor.getOrNull(INDEX_SUB)?.AsString(),
        cbor.decodeCertificate()
    )

    companion object {
        const val INDEX_ISS = 1
        const val INDEX_SUB = 2
        const val INDEX_EXP = 4
        const val INDEX_NBF = 5
        const val INDEX_IAT = 6
        const val INDEX_HCERT = -260
    }
}

private fun CBORObject.decodeCertificate(): Certificate? {
    val hcert = this[CWT.INDEX_HCERT]
    val hcertv1 = hcert[CBORObject.FromObject(1)].EncodeToBytes()

    return CBORMapper().readValue(hcertv1, Certificate::class.java)
}
