package com.merative.healthpass.ui.settings.sound

import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.SharedPrefUtils
import javax.inject.Inject

class SoundVibrationVM @Inject constructor(
    private val sharedPrefUtils: SharedPrefUtils,
) : BaseViewModel() {

    fun isSoundEnabled(): Boolean {
        return sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_SOUND_FEEDBACK)
    }

    fun setSoundEnabled(enabled: Boolean) {
        sharedPrefUtils.putBoolean(SharedPrefUtils.KEY_SOUND_FEEDBACK, enabled, true)
    }

    fun isHapticEnabled(): Boolean {
        return sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_HAPTIC_FEEDBACK)
    }

    fun setHapticEnabled(enabled: Boolean) {
        sharedPrefUtils.putBoolean(SharedPrefUtils.KEY_HAPTIC_FEEDBACK, enabled, true)
    }
}