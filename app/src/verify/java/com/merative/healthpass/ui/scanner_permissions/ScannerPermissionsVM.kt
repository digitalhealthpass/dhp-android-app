package com.merative.healthpass.ui.scanner_permissions

import androidx.lifecycle.ViewModel
import com.merative.healthpass.utils.pref.SharedPrefUtils
import javax.inject.Inject

class ScannerPermissionsVM @Inject constructor (
    val sharedPrefUtils: SharedPrefUtils,
    ): ViewModel() {

    fun onPermissionDenied(){
        sharedPrefUtils.putBoolean(SharedPrefUtils.KEY_CAMERA_PERMISSION, true)
    }

    fun isPermissionDenied() = sharedPrefUtils.getBoolean(SharedPrefUtils.KEY_CAMERA_PERMISSION)
}