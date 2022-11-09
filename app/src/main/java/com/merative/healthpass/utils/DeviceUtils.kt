package com.merative.healthpass.utils

import android.content.Context
import android.os.Build
import com.merative.healthpass.BuildConfig
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.extensions.defaultSharedPre
import java.io.File

object DeviceUtils {
    fun isProhibitedRoot(context: Context): Boolean {
        val isEmulator = isEmulator()
        if (BuildConfig.DEBUG
            && context.defaultSharedPre.getBoolean(AppConstants.KEY_PREF_ROOT_CHECK, false)
        ) {
            return false
        }
        return isRooted()
    }

    private fun isRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    fun isEmulator(): Boolean {
//        CommonUtils.isEmulator(context)
        // Checking to see if device is an emulator or physical device
        return (Build.FINGERPRINT.startsWith("google/sdk_gphone_")
                && Build.FINGERPRINT.endsWith(":user/release-keys")
                && Build.MANUFACTURER == "Google" && Build.PRODUCT.startsWith("sdk_gphone_") && Build.BRAND == "google"
                && Build.MODEL.startsWith("sdk_gphone_"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || "QC_Reference_Phone" == Build.BOARD
                && !"Xiaomi".equals(Build.MANUFACTURER, ignoreCase = true)
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.HOST.startsWith("Build") //MSI App Player
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || Build.PRODUCT == "google_sdk"
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
    }
}