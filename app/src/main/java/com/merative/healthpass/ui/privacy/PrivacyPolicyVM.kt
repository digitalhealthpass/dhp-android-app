package com.merative.healthpass.ui.privacy

import androidx.lifecycle.ViewModel
import com.merative.healthpass.utils.pref.SharedPrefUtils
import javax.inject.Inject

class PrivacyPolicyVM @Inject constructor(
    val sharedPrefUtils: SharedPrefUtils
) : ViewModel() {

    fun privacyAccepted() {
        sharedPrefUtils.putBoolean(SharedPrefUtils.KEY_PRIVACY_ACCEPTED, true)
    }
}