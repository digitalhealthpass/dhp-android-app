package com.merative.healthpass.ui.tutorial

import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.utils.pref.SharedPrefUtils
import javax.inject.Inject

class TutorialVM @Inject constructor(
    private val sharedPrefUtils: SharedPrefUtils
) : BaseViewModel() {

    fun tutorialDone() {
        sharedPrefUtils.putBoolean(SharedPrefUtils.KEY_TUTORIAL_SHOWN, true)
    }
}