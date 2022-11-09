package com.merative.watson.healthpass.utils

import android.graphics.Bitmap
import com.merative.watson.healthpass.interfaces.Consumer
import com.merative.watson.healthpass.scan.BarcodeScanner

class QRCodeDecoder<T>(
    val onSuccessListener: Consumer<DecoderResult<T>>? = null,
    val onErrorListener: Consumer<Exception>? = null
) {
    private val barcoderScanner = BarcodeScanner(onSuccessListener, onErrorListener)

    fun decodeBitmap(bitmap: Bitmap, item: T? = null) {
        barcoderScanner.start()
        barcoderScanner.processImageProxy(bitmap, 0, item)
    }

    companion object {
        private const val TAG = "QRCodeDecoder"

    }
}