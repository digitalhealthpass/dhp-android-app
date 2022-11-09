package com.merative.healthpass.ui.mainActivity

import androidx.lifecycle.ViewModel
import com.merative.healthpass.utils.pref.SharedPrefUtils
import javax.inject.Inject

class MainActivityVM @Inject constructor(
    private val sharedPrefUtils: SharedPrefUtils,
) : ViewModel() {
    var isPinValidationRequired = true
        private set

    init {
        if (hasPinSkipped())
            isPinValidationRequired = false
    }

    fun validationFinished() {
        isPinValidationRequired = false
    }

    fun reset() {
        isPinValidationRequired = true
    }

    private fun hasPinSkipped(): Boolean = sharedPrefUtils.hasPinSkipped()
}