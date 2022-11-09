package com.merative.healthpass.ui.landing

import androidx.lifecycle.ViewModel
import com.merative.healthpass.common.dagger.AppFlavorActions
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.pref.*
import com.merative.healthpass.utils.pref.SharedPrefUtils.Companion.KEY_KIOSK_AUTO_DISMISS
import com.merative.healthpass.utils.pref.SharedPrefUtils.Companion.KEY_KIOSK_AUTO_DISMISS_DURATION
import com.merative.healthpass.utils.pref.SharedPrefUtils.Companion.KEY_KIOSK_FRONT_CAMERA
import com.merative.healthpass.utils.pref.SharedPrefUtils.Companion.KEY_KIOSK_MODE
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class LandingViewModel @Inject constructor(
    private val environmentHandler: EnvironmentHandler,
    val sharedPrefUtils: SharedPrefUtils,
    private val contactDB: ContactDB,
    private val schemaDB: SchemaDB,
    private val metadataDB: IssuerMetadataDB,
    private val issuerDB: IssuerDB,
    private val appFlavorActions: AppFlavorActions,
) : ViewModel() {
    fun isTermsAccepted(): Boolean {
        return sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_TERMS_ACCEPTED)
    }

    fun isPrivacyAccepted(): Boolean {
        return sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_PRIVACY_ACCEPTED)
    }

    fun canShowTutorial(): Boolean {
        return !sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_TUTORIAL_SHOWN)
    }

    fun shouldDisableFeatureForRegion() = environmentHandler.shouldDisableFeatureForRegion()

    fun hasPin(): Boolean = sharedPrefUtils.hasPin()

    fun hasPinSkipped(): Boolean = sharedPrefUtils.hasPinSkipped()

    fun isTimeToResetCache(): Boolean {
        val autoReset = sharedPrefUtils.getAutoReset()
        if (autoReset.isEnabled and autoReset.isTimeToReset()) {
            return true
        }
        return false
    }

    fun resetCache(): Completable {
        return schemaDB.deleteAll()
            .andThen(metadataDB.deleteAll())
            .andThen(issuerDB.deleteAll())
            .andThen {
                sharedPrefUtils.setAutoReset(sharedPrefUtils.getAutoReset(), true)
                it.onComplete()
            }

    }

    fun isEnvRegionSet(): Boolean = environmentHandler.isEnvRegionSet()

    fun setKioskModeEnabled(enabled: Boolean) {
        if (!sharedPrefUtils.contains(KEY_KIOSK_MODE)) {
            sharedPrefUtils.putBoolean(KEY_KIOSK_MODE, enabled, true)
        }
    }

    fun setDefaultFrontCamera(enabled: Boolean) {
        if (!sharedPrefUtils.contains(KEY_KIOSK_FRONT_CAMERA)) {
            sharedPrefUtils.putBoolean(KEY_KIOSK_FRONT_CAMERA, enabled, true)
        }
    }

    fun setAutoDismiss(enabled: Boolean) {
        if (!sharedPrefUtils.contains(KEY_KIOSK_AUTO_DISMISS)) {
            sharedPrefUtils.putBoolean(KEY_KIOSK_AUTO_DISMISS, enabled, true)
        }
    }

    fun setAutoDismissDuration() {
        if (!sharedPrefUtils.contains(KEY_KIOSK_AUTO_DISMISS_DURATION)) {
            sharedPrefUtils.putInt(KEY_KIOSK_AUTO_DISMISS_DURATION, DEFAULT_AUTO_DISMISS_DURATION)
        }
    }

    companion object {

        const val DEFAULT_AUTO_DISMISS_DURATION = 5
    }
}