package com.merative.healthpass.utils

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient


internal abstract class TimeoutWebViewClient : WebViewClient() {
    var timeout = false

    open fun MyWebViewClient() {
        timeout = true
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        Thread {
            timeout = true
            try {
                Thread.sleep(10000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            if (timeout) {
                // do what you want
            }
        }.start()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        timeout = false
    }
}