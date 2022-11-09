package com.merative.healthpass.models.pin

import com.merative.healthpass.extensions.isNotNullOrEmpty

data class PinCode(val code: String?, val isBiometric: Boolean?) {
    companion object {
        val EMPTY = PinCode(null, null)
        val SKIPPED = PinCode(null, false)
    }
}

/**
 * @return true if isBiometric is true OR code NotNullOrEmpty
 */
fun PinCode?.hasPinCode(): Boolean {
    return this?.isBiometric == true || this?.code.isNotNullOrEmpty()
}