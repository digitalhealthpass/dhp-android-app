package com.merative.watson.healthpass.scan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.merative.watson.healthpass.R
import com.merative.watson.healthpass.databinding.FragmentScanBinding
import com.merative.watson.healthpass.extension.showDialog
import com.merative.watson.healthpass.interfaces.Consumer
import com.merative.watson.healthpass.models.NetworkAvailability
import com.merative.watson.healthpass.utils.DecoderResult

open class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    protected val binding get() = _binding!!

    private val requestCameraPermissionCode = 1001

    private val successListener = object : Consumer<DecoderResult<Unit>> {
        override fun onNext(result: DecoderResult<Unit>) {
            detectQRCode(result.result)
        }
    }

    private var cameraPresenter: CameraPresenter? = null
    private var barcodeProcessor: BarcodeScanner<Unit>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        barcodeProcessor = BarcodeScanner(successListener)
        cameraPresenter =
            CameraPresenter(barcodeProcessor!!, requireContext(), this, binding.previewView)

        binding.buttonAuthorizeCamera.setOnClickListener {
            showPermissionDialog()
        }
    }

    private fun requestPermissionOrSetupCamera() {
        if (!hasCameraPermission()) {
            showPermissionDialog()
        } else {
            setupControls()
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupControls() {
        binding.previewView.isVisible = true
        binding.permissionContainer.isGone = true
        cameraPresenter?.start()

        configTorchButton()
        configCameraButton()
    }

    private fun configTorchButton() {
        val canShowTorch = canShowTorch()

        binding.scanImgFlash.isVisible =
            canShowTorch && cameraPresenter?.isLedFlashAvailable == true

        binding.scanImgFlash.isChecked = cameraPresenter?.isFlashOn ?: false
        binding.scanImgFlash.setOnCheckedChangeListener { _, isChecked ->
            cameraPresenter?.toggleFlash(isChecked)
        }
    }

    private fun configCameraButton() {
        val canToggleCamera = arguments?.getBoolean(KEY_TOGGLE_CAMERA) ?: false

        binding.scanImgCamera.isVisible =
            canToggleCamera && cameraPresenter?.isFrontCameraAvailable == true
        binding.scanImgCamera.setOnClickListener {
            cameraPresenter?.toggleCamera()
            binding.scanImgFlash.isVisible =
                canShowTorch() && cameraPresenter?.isLedFlashAvailable == true
        }
    }

    private fun canShowTorch() = arguments?.getBoolean(KEY_SHOW_TORCH) ?: false

    /**
     * this is being called after checking that the app does not have camera permission
     */
    protected open fun showPermissionDialog() {
        activity?.showDialog(
            getString(R.string.camera_access_permission_title),
            getString(R.string.camera_access_permission_text),
            getString(R.string.allow),
            {
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    requestCameraPermissionCode
                )
            },
            getString(R.string.decline), { showDeclinedCameraPermissionUi() })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == requestCameraPermissionCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.permissionContainer.isVisible = false
                setupControls()
            } else {
                activity?.showDialog(
                    null,
                    getString(R.string.permission_denied),
                    getString(R.string.ok), {},
                    "", {}
                )
                showDeclinedCameraPermissionUi()
            }
        }
    }

    private fun showDeclinedCameraPermissionUi() {
        binding.previewView.isVisible = false
        binding.permissionContainer.isVisible = true
    }

    @Synchronized
    protected open fun detectQRCode(barCodeString: String?) {
        try {
            if (barCodeString != null) {
                stopBarcodeRecognition()
                activity?.runOnUiThread {
                    Log.d(
                        TAG, "QR parsed: $barCodeString"
                    )
                    setFragmentResult(
                        KEY_RESULT,
                        bundleOf(
                            KEY_QR_SCHEMA_JSON to barCodeString.orEmpty()
                        )
                    )
                    findNavController().popBackStack()
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "invalid QR code", ex)
            activity?.runOnUiThread {
                findNavController().popBackStack()
                Toast.makeText(
                    activity,
                    getString(R.string.invalid_qr),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    protected fun resumeBarcodeRecognition() {
        cameraPresenter?.resumeBarcodeRecognition()
    }

    protected fun stopBarcodeRecognition() {
        cameraPresenter?.stopBarcodeRecognition()
    }

    override fun onResume() {
        super.onResume()
        requestPermissionOrSetupCamera()
        configTorchButton()
        hasInternetConnection()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        barcodeProcessor = null
        cameraPresenter = null
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val KEY_SHOW_TORCH = "allow_torch"
        const val KEY_TOGGLE_CAMERA = "toggle_camera"

        private const val TAG = "ScanFragment"
        const val KEY_RESULT = "result"
        const val KEY_QR_SCHEMA_JSON = "qr_schema_json"

        private fun startQRCodeScan(activity: Activity) {
            val intent = Intent("com.google.zxing.client.android.SCAN")
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE")
            intent.putExtra("CHARACTER_SET", "ISO-8859-1")
            activity.startActivityForResult(intent, 0)


            val result = intent.getStringExtra("SCAN_RESULT")!!.toByteArray(charset("ISO-8859-1"))

        }
    }

    fun hasInternetConnection() {
        activity?.let {
            ConnectionStateLiveData.get(it).observe(viewLifecycleOwner, Observer {
                notifyOfflineMode((it == NetworkAvailability.CONNECTED))
            })
        }
    }

    protected open fun notifyOfflineMode(boolean: Boolean) {
    }
}