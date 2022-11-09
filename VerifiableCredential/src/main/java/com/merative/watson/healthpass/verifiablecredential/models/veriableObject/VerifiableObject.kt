package com.merative.watson.healthpass.verifiablecredential.models.veriableObject

import android.os.Parcelable
import android.util.Log
import com.google.gson.JsonElement
import com.merative.watson.healthpass.verifiablecredential.extensions.format
import com.merative.watson.healthpass.verifiablecredential.extensions.timestampToDate
import com.merative.watson.healthpass.verifiablecredential.extensions.toJsonElement
import com.merative.watson.healthpass.verifiablecredential.interfaces.Exclude
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.cose.Cose
import com.merative.watson.healthpass.verifiablecredential.models.cose.cwt
import com.merative.watson.healthpass.verifiablecredential.models.credential.Credential
import com.merative.watson.healthpass.verifiablecredential.models.jws.JWS
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject.Companion.TAG
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.concurrent.TimeUnit

@Parcelize
class VerifiableObject(val rawString: String) : Parcelable {

    @Exclude
    @IgnoredOnParcel
    var jws: JWS? = null

    @Exclude
    @IgnoredOnParcel
    var type: VCType = VCType.UNKNOWN

    @Exclude
    @IgnoredOnParcel
    var cose: Cose? = null

    @Exclude
    @IgnoredOnParcel
    var credential: Credential? = null

    @Exclude
    @IgnoredOnParcel
    var isRulesUnknown = false

    init {
        if (isValidSHC(this.rawString)) {//smarthealth.cards
            val scannedJWS = if (!this.rawString.startsWith(SHC_PREFIX)) {
                this.rawString
            } else {
                parseSHC(this.rawString)
            }
            Log.d(TAG, scannedJWS.toString())

            if (scannedJWS != null && isValidJWS(scannedJWS)) {
                jws = JWS(scannedJWS)
                jws?.let { type = VCType.SHC }
            }
        } else if (isValidHC1(this.rawString)) {
            val scannedHC1 = parseHC1(this.rawString)
            Log.d(TAG, scannedHC1)

            cose = Cose(scannedHC1)
            cose?.let { type = VCType.DCC }
        } else if (isValidCredential(rawString)) { // DigitalHealthPass or GoodHealthPass
            credential = getCredential(rawString)
            type = getCredentialType(credential)
        } else {
            type = VCType.UNKNOWN
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VerifiableObject

        if (rawString != other.rawString) return false

        return true
    }

    override fun hashCode(): Int {
        return rawString.hashCode()
    }

    companion object {
        const val HC1_PREFIX: String = "HC1:"
        const val SHC_PREFIX: String = "shc:/"
        const val JWS_BODY_SIZE: Int = 3
        const val TAG = "VerifiableObject"
    }
}

val VerifiableObject.typeForDisplay: String
    get() {
        return if (isRulesUnknown) {
            type.displayValue + " (Unknown)"
        } else {
            type.displayValue
        }
    }

val VerifiableObject.payLoad: JsonElement?
    get() {
        return when (type) {
            VCType.SHC -> {
                jws?.payload?.toJsonElement()
            }
            VCType.IDHP, VCType.GHP, VCType.DIVOC, VCType.VC -> {
                credential?.toJsonElement()
            }
            VCType.DCC -> {
                cose?.hCertJson?.toJsonElement()
            }
            else -> {
                null
            }
        }
    }

val VerifiableObject.isIDHPorGHPorVC: Boolean
    get() = type == VCType.IDHP || type == VCType.GHP || type == VCType.VC

val VerifiableObject.expiryDate: String
    get() = when (type) {
        VCType.IDHP, VCType.GHP, VCType.DIVOC, VCType.VC -> {
            credential?.getFormattedExpiryDate()
                .orEmpty()
        }
        VCType.SHC -> {
            jws?.payload?.exp?.toLong()?.let { TimeUnit.SECONDS.toMillis(it) }
                ?.timestampToDate()
                ?.format(dateFormat = Credential.OUTPUT_DATE_FORMAT)
                .orEmpty()
        }
        VCType.DCC -> {
            cose?.cwt?.exp?.let { TimeUnit.SECONDS.toMillis(it) }
                ?.timestampToDate()
                ?.format(dateFormat = Credential.OUTPUT_DATE_FORMAT)
                .orEmpty()
        }
        else -> {
            Log.i(TAG, "Expiration - Unknown")
            ""
        }
    }

val VerifiableObject.issuanceDate: String
    get() = when (type) {
        VCType.IDHP, VCType.GHP, VCType.DIVOC, VCType.VC -> {
            credential?.getFormattedIssuedDate()
                .orEmpty()
        }
        VCType.SHC -> {
            jws?.payload?.iat?.toLong()?.let { TimeUnit.SECONDS.toMillis(it) }
                ?.timestampToDate()
                ?.format(dateFormat = Credential.OUTPUT_DATE_FORMAT)
                .orEmpty()
        }
        VCType.DCC -> {
            cose?.cwt?.iat?.let { TimeUnit.SECONDS.toMillis(it) }
                ?.timestampToDate()
                ?.format(dateFormat = Credential.OUTPUT_DATE_FORMAT)
                .orEmpty()
        }
        else -> {
            Log.i(TAG, "Expiration - Unknown")
            ""
        }
    }