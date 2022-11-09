package com.merative.healthpass.common.dagger

import android.content.Context

class AppFlavorActions (context: Context) {

    fun isDeepLinkEnabled(): Boolean{
        return true
    }
}