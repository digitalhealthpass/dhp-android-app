package com.merative.healthpass.ui.terms

import androidx.lifecycle.ViewModel
import com.merative.healthpass.utils.pref.SharedPrefUtils
import javax.inject.Inject

class TermsViewModel @Inject constructor(
    private val sharedPrefUtils: SharedPrefUtils
) : ViewModel() {

    fun termsAccepted() {
        sharedPrefUtils.putBoolean(SharedPrefUtils.KEY_TERMS_ACCEPTED, true)
    }

    fun termsDeclined() {
        sharedPrefUtils.putBoolean(SharedPrefUtils.KEY_TERMS_ACCEPTED, false)
    }
}