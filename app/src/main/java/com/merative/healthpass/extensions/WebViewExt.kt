package com.merative.healthpass.extensions

import android.content.res.Configuration
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.StringRes
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.merative.healthpass.BuildConfig

fun WebView.initCommonFeatures() {
    adjustForDarkMode()
    settings.javaScriptEnabled = true
    settings.loadWithOverviewMode = true
    settings.useWideViewPort = true
    settings.setSupportZoom(true)
    settings.builtInZoomControls = true
    settings.displayZoomControls = false
//    scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
//    isScrollbarFadingEnabled = false

    adjustForLogs()
}

fun WebView.adjustForDarkMode() {
    if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                //enable force dark
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON)
            }
            Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                //disable force dark
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_OFF)
            }
            else -> {
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_AUTO)
            }
        }
    }
}

fun WebView.adjustForLogs() {
    if (BuildConfig.DEBUG) {
        WebView.setWebContentsDebuggingEnabled(true)

        // The following two lines help with disabling asset caching
        settings.setAppCacheEnabled(false)
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
    }
}

fun WebView.loadStringRes(@StringRes resId: Int) {
    loadDataWithBaseURL(
        null, context.getString(resId), "text/html", "UTF-8", null
    )
}