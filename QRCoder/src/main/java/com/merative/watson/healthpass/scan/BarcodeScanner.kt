package com.merative.watson.healthpass.scan

import android.graphics.Bitmap
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.merative.watson.healthpass.exception.NoDisplayValueException
import com.merative.watson.healthpass.interfaces.Consumer
import com.merative.watson.healthpass.utils.DecoderResult

class BarcodeScanner<T>(
    private val onSuccessListener: Consumer<DecoderResult<T>>?,
    private val onErrorListener: Consumer<Exception>? = null
) {
    private lateinit var options: BarcodeScannerOptions
    private lateinit var barcodeScanner: BarcodeScanner

    fun start() {
        stop()
        options = BarcodeScannerOptions.Builder().build()
        barcodeScanner = BarcodeScanning.getClient(options)
    }

    fun stop() {
        if (::barcodeScanner.isInitialized) {
            barcodeScanner.close()
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    fun processImageProxy(proxyImage: ImageProxy) {
        Log.d(TAG, "--- proxyImage: ${proxyImage.image}")

        proxyImage.image?.let { image ->
            barcodeScanner.process(InputImage.fromMediaImage(image, 0))
                .addOnSuccessListener { processRecognitionSuccess(it) }
                .addOnFailureListener { processRecognitionError(it) }
                .addOnCompleteListener { onRecognitionCompleted(image, proxyImage) }
        }
    }

    fun processImageProxy(bitmap: Bitmap, rotation: Int, item: T? = null) {
        barcodeScanner.process(InputImage.fromBitmap(bitmap, rotation))
            .addOnSuccessListener { processRecognitionSuccess(it, item) }
            .addOnFailureListener { processRecognitionError(it) }
    }

    private fun processRecognitionSuccess(list: MutableList<Barcode>, item: T? = null) {
        val value = list.firstOrNull()
        Log.i(TAG, "--- Recognition result: ${value?.displayValue}")

        value?.displayValue?.let { onSuccessListener?.onNext(DecoderResult(it, item)) }
            ?: onErrorListener?.onNext(NoDisplayValueException("DisplayValue is null"))
    }

    private fun processRecognitionError(exception: Exception) {
        Log.e(TAG, "--- Recognition failed", exception)
        onErrorListener?.onNext(exception)
    }

    private fun onRecognitionCompleted(image: Image, proxyImage: ImageProxy) {
        Log.i(TAG, "--- onRecognitionCompleted")
        image.close()
        proxyImage.close()
    }

    companion object {
        const val TAG = "ImageProcessor"
    }
}