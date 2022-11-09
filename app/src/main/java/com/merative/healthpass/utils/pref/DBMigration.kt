package com.merative.healthpass.utils.pref

import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.extensions.isNotNullOrEmpty
import com.merative.healthpass.models.pin.PinCode
import com.merative.watson.healthpass.verifiablecredential.extensions.stringfy
import javax.inject.Inject

/**
 * This class should merge Key DB with Contact DB, and update PIN code from a string to PinCode Model
 * version 102 had [AsymmetricKeyDB] and was merged with [ContactDB] in 103
 */
class DBMigration @Inject constructor(
    private val contactKeyDBMigration: ContactKeyDBMigration,
    private val sharedPrefUtils: SharedPrefUtils,
) {
    fun start() {
        contactKeyDBMigration.start()
        updatePinDB()
    }

    private fun updatePinDB() {
        val keyPinOld = "pin_code"
        val oldPin = sharedPrefUtils.getString(keyPinOld)
        val pinCode: PinCode? = when {
            oldPin == AppConstants.BIOMETRIC_PIN -> {
                PinCode(null, true)
            }
            oldPin?.isEmpty() == true -> {
                //it means it was skipped
                PinCode.SKIPPED
            }
            oldPin?.isNotEmpty() == true -> {
                PinCode(oldPin, false)
            }
            else -> null
        }
        if (pinCode != null) {
            sharedPrefUtils.putString(SharedPrefUtils.KEY_PIN, pinCode.stringfy())
        }

        if (oldPin.isNotNullOrEmpty()) {
            sharedPrefUtils.clearPrefs(keyPinOld)
        }
    }
}