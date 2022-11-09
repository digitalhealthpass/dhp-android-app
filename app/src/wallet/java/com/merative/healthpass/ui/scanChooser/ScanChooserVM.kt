package com.merative.healthpass.ui.scanChooser

import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.pref.SharedPrefUtils
import javax.inject.Inject

class ScanChooserVM @Inject constructor(
    private val sharedPrefUtils: SharedPrefUtils,
    private val environmentHandler: EnvironmentHandler,
) : BaseViewModel() {

    fun isFileAccessGranted(): Boolean = sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_FILE_ACCESS)

    fun fileAccessGranted() = sharedPrefUtils.putBoolean(SharedPrefUtils.KEY_FILE_ACCESS, true)

    fun shouldDisableFeatureForRegion() = environmentHandler.shouldDisableFeatureForRegion()
}