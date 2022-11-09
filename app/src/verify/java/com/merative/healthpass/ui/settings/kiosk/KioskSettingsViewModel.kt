package com.merative.healthpass.ui.settings.kiosk

import com.merative.healthpass.models.Duration
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.SharedPrefUtils
import com.merative.healthpass.utils.pref.SharedPrefUtils.Companion.KEY_KIOSK_AUTO_DISMISS
import com.merative.healthpass.utils.pref.SharedPrefUtils.Companion.KEY_KIOSK_AUTO_DISMISS_DURATION
import com.merative.healthpass.utils.pref.SharedPrefUtils.Companion.KEY_KIOSK_FRONT_CAMERA
import com.merative.healthpass.utils.pref.SharedPrefUtils.Companion.KEY_KIOSK_MODE
import javax.inject.Inject

class KioskSettingsViewModel @Inject constructor(
    private val sharedPrefUtils: SharedPrefUtils,
) : BaseViewModel() {

    fun isKioskModeEnabled(): Boolean {
        return sharedPrefUtils.getBoolean(KEY_KIOSK_MODE)
    }

    fun setKioskModeEnabled(enabled: Boolean) {
        sharedPrefUtils.putBoolean(KEY_KIOSK_MODE, enabled, true)
    }

    fun getAutoDismiss(): Boolean {
        return sharedPrefUtils.getBoolean(KEY_KIOSK_AUTO_DISMISS)
    }

    fun setDefaultFrontCamera(enabled: Boolean) {
        sharedPrefUtils.putBoolean(KEY_KIOSK_FRONT_CAMERA, enabled, true)
    }

    fun getDefaultFrontCamera(): Boolean {
        return sharedPrefUtils.getBoolean(KEY_KIOSK_FRONT_CAMERA)
    }

    fun setAutoDismiss(enabled: Boolean) {
        sharedPrefUtils.putBoolean(KEY_KIOSK_AUTO_DISMISS, enabled, true)
    }

    fun getAutoDismissDuration(): Int {
        return sharedPrefUtils.getInt(KEY_KIOSK_AUTO_DISMISS_DURATION)
    }

    fun setAutoDismissDuration(duration: Int) {
        sharedPrefUtils.putInt(KEY_KIOSK_AUTO_DISMISS_DURATION, duration)
    }

    fun getDurationList(): List<Duration> {
        return mutableListOf(Duration(3), Duration(5), Duration(10))
    }
}