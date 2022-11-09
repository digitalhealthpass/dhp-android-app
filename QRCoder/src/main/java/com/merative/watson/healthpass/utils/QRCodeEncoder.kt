package com.merative.watson.healthpass.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.annotation.CheckResult
import androidx.annotation.WorkerThread
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class QRCodeEncoder {
    private val TAG = "QRCodeEncoder"

    /**
     * This requires a bit of calculation so it is better to run on different thread, then use the bitmap on MainThread
     */
    @WorkerThread
    @CheckResult
    fun encode(
        qrCodeJson: String,
        size: Int = 300,
        color: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap {
        val screenWidth: Int = Resources.getSystem().displayMetrics.widthPixels
        val targetSize = if (screenWidth < size) {
            Log.w(
                TAG,
                "encode: your size: $size is bigger than the screen width $screenWidth, falling back to screen width"
            )
            screenWidth
        } else {
            size
        }

//        val hintMap = Hashtable<EncodeHintType, String>()
//        hintMap[EncodeHintType.CHARACTER_SET] = "UTF-8"

        val writer = QRCodeWriter()
        val matrix = writer.encode(qrCodeJson, BarcodeFormat.QR_CODE, targetSize, targetSize)
        val bmp = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.RGB_565)

        var x = 0
        while (x < targetSize) {
            var y = 0
            while (y < targetSize) {
                bmp.setPixel(
                    x,
                    y,
                    if (matrix.get(x, y)) color else backgroundColor
                )
                y++
            }
            x++
        }
        return bmp
    }
}