package com.merative.watson.healthpass.scan

import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.*
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

@androidx.camera.core.ExperimentalGetImage
class CameraPresenter(
    private val barcodeProcessor: BarcodeScanner<Unit>,
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) {

    val isFlashOn: Boolean
        get() = camera?.cameraInfo?.torchState?.value == TorchState.ON

    val isLedFlashAvailable: Boolean
        get() = (camera?.cameraInfo?.hasFlashUnit() ?: false) && lensFacing == LENS_FACING_BACK

    val isFrontCameraAvailable: Boolean
        get() = cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false

    private var lensFacing = LENS_FACING_BACK

    private var previewUseCase: Preview? = null
    private var camera: Camera? = null

    private var cameraSelector: CameraSelector? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var analysisUseCase: ImageAnalysis? = null

    fun start() {
        cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        cameraProvider = ProcessCameraProvider.getInstance(context).get()

        bindAllCameraUseCases()
    }

    fun toggleFlash(enabled: Boolean) {
        if (cameraProvider != null && lensFacing == LENS_FACING_BACK) {
            camera?.cameraControl?.enableTorch(enabled)
        }
    }

    fun toggleCamera() {
        if (cameraProvider != null) {
            val newLensFacing = getLensFacing()
            val cameraSelector = CameraSelector.Builder().requireLensFacing(newLensFacing).build()
            try {
                if (cameraProvider!!.hasCamera(cameraSelector)) {
                    lensFacing = newLensFacing
                    this.cameraSelector = cameraSelector
                    bindAllCameraUseCases()
                }
            } catch (e: CameraInfoUnavailableException) {
                Log.e(TAG, e.message, e)
            }
        }
    }

    private fun getLensFacing() =
        if (lensFacing == LENS_FACING_FRONT) LENS_FACING_BACK else LENS_FACING_FRONT


    private fun bindAllCameraUseCases() {
        cameraProvider?.let {
            it.unbindAll()
            bindPreviewUseCase()
            bindAnalysisUseCase()
        }
    }

    private fun bindPreviewUseCase() {
        if (cameraProvider == null) {
            return
        }
        previewUseCase?.let { cameraProvider!!.unbind(it) }

        previewUseCase = Preview.Builder().setTargetResolution(Size(WIDTH, HEIGHT)).build()
        previewUseCase!!.setSurfaceProvider(previewView.surfaceProvider)
        camera = cameraProvider!!.bindToLifecycle(lifecycleOwner, cameraSelector!!, previewUseCase)
    }

    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        cameraProvider!!.unbind(analysisUseCase)
        barcodeProcessor.start()

        analysisUseCase = ImageAnalysis.Builder().setTargetResolution(Size(WIDTH, HEIGHT)).build()
        analysisUseCase?.setAnalyzer(
            ContextCompat.getMainExecutor(context),
            { proxy -> barcodeProcessor.processImageProxy(proxy) }
        )
        cameraProvider!!.bindToLifecycle(lifecycleOwner, cameraSelector!!, analysisUseCase)
    }

    fun stopBarcodeRecognition() {
        cameraProvider?.let {
            barcodeProcessor.stop()
            it.unbind(analysisUseCase)
        }
    }

    fun resumeBarcodeRecognition() {
        bindAnalysisUseCase()
    }

    companion object {

        const val TAG = "CameraPresenter"

        const val WIDTH = 1080
        const val HEIGHT = 1920
    }
}