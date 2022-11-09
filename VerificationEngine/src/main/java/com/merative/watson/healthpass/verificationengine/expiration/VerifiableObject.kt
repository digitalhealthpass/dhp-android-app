package com.merative.watson.healthpass.verificationengine.expiration

import android.util.Log
import com.merative.watson.healthpass.verifiablecredential.extensions.getCurrentDateInUtcFormat
import com.merative.watson.healthpass.verifiablecredential.extensions.timestampToDate
import com.merative.watson.healthpass.verifiablecredential.models.cose.cwt
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import com.merative.watson.healthpass.verificationengine.VerifyEngine
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "VerifiableObjectExpiration"

fun VerifiableObject.isExpired(): Boolean {
    when {
        credential != null -> {
            return credential?.isExpired() ?: true
        }
        jws != null -> {
            val expDateTimeInterval =
                jws?.payload?.exp?.toLong()?.let { TimeUnit.SECONDS.toMillis(it) } ?: return false

            expDateTimeInterval.let {
                val expirationDate = it.timestampToDate()
                return expirationDate != null && expirationDate < Date().getCurrentDateInUtcFormat()
            }
        }
        cose != null -> {
            val expDateTimeInterval = cose?.cwt?.exp?.let { TimeUnit.SECONDS.toMillis(it) }
            expDateTimeInterval?.let {
                val expirationDate = it.timestampToDate()
                return expirationDate != null && expirationDate < Date().getCurrentDateInUtcFormat()
            }

            Log.i(TAG, "isExpired: COSE - Expired or Missing")
            return true
        }
        else -> {
            Log.i(TAG, "Expiration - Unknown")
            return true
        }
    }
}

class ExpiredException(val verifyEngine: VerifyEngine) : Exception()