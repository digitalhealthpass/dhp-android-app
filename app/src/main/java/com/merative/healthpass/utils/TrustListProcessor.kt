package com.merative.healthpass.utils

import com.merative.healthpass.models.TrustedIssuer
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import org.json.JSONObject

class TrustListProcessor(private val trustListJson: String) {

    fun getNameFromTrustList(verifiableObject: VerifiableObject): String {
        if (verifiableObject.type == VCType.SHC) {
            val json = JSONObject(trustListJson)
            val issuers = json.getJSONArray("participating_issuers").toString()

            val trustedIssuersList = parse<List<TrustedIssuer>>(issuers)

            return trustedIssuersList.firstOrNull { it.iss == verifiableObject.jws?.payload?.iss }?.name
                ?: ""

        } else {
            throw IllegalStateException()
        }
    }

}