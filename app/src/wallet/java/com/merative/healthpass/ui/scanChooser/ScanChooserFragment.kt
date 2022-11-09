package com.merative.healthpass.ui.scanChooser

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.merative.healthpass.R
import com.merative.healthpass.databinding.ScanChooserFragmentBinding
import com.merative.healthpass.extensions.applyAppBarLayout
import com.merative.healthpass.extensions.logd
import com.merative.healthpass.extensions.showDialog
import com.merative.healthpass.extensions.snackShort
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.scanVerify.ScanVerifyFragment
import com.merative.watson.healthpass.exception.NoDisplayValueException
import com.merative.watson.healthpass.interfaces.Consumer
import com.merative.watson.healthpass.scan.QRCodeImageParser
import com.merative.watson.healthpass.scan.ScanFragment
import com.merative.watson.healthpass.utils.DecoderResult
import com.merative.watson.healthpass.verifiablecredential.exception.InvalidCredentialTypeException
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import org.json.JSONException
import java.io.FileNotFoundException

class ScanChooserFragment : BaseFragment() {
    private var _binding: ScanChooserFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var viewModel: ScanChooserVM

    private val qRCodeImageParser: QRCodeImageParser<Unit> by lazy {
        QRCodeImageParser(this)
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { qRCodeImageParser.processUri(it) } ?: logd("uri == null")
        }

    private lateinit var singlePermission: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        singlePermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    qRCodeImageParser.start()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = ScanChooserFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = createViewModel(ScanChooserVM::class.java)

        binding.btnQrCamera.setOnClickListener {
            applyAppBarLayout { setExpanded(false) }
            findNavController().navigate(
                R.id.global_action_to_scan_fragment, bundleOf(
                    ScanFragment.KEY_SHOW_TORCH to false
                )
            )
        }

        listenToScanQRCode()

        binding.btnQrImage.setOnClickListener {
            if (!viewModel.isFileAccessGranted()) {
                activity?.showDialog(
                    getString(R.string.file_access_permission_title),
                    getString(R.string.file_access_permission_text),
                    getString(R.string.allow),
                    {
                        viewModel.fileAccessGranted()
                        requestStoragePermission()
                    },
                    getString(R.string.decline),
                    {}
                )
            } else {
                requestStoragePermission()
            }
        }

        listenToDidEvents()
    }

    private fun requestStoragePermission() {
        singlePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun listenToDidEvents() {
        qRCodeImageParser.onSuccessListener = object : Consumer<DecoderResult<Unit>> {
            override fun onNext(result: DecoderResult<Unit>) {
                handleData(result.result)
            }
        }

        qRCodeImageParser.onErrorListener = object : Consumer<Exception> {
            override fun onNext(data: Exception) {
                when (data) {
                    is NoDisplayValueException, is JSONException -> {
                        activity?.showDialog(
                            null,
                            getString(R.string.qr_invalid_file_data),
                            getString(R.string.button_title_ok)
                        ) {}
                    }
                    is FileNotFoundException -> {
                        activity?.showDialog(
                            null,
                            getString(R.string.qr_invalid_file),
                            getString(R.string.button_title_ok)
                        ) {}
                    }
                    else -> {
                        activity?.showDialog(
                            null,
                            getString(R.string.qr_invalid_file),
                            getString(R.string.button_title_ok)
                        ) {}
                    }
                }
            }
        }
    }

    private fun listenToScanQRCode() {
        setFragmentResultListener(
            ScanFragment.KEY_RESULT
        ) { _, bundle ->
            val qrCredentialJson = bundle.getString(ScanFragment.KEY_QR_SCHEMA_JSON, "")
            handleData(qrCredentialJson)
        }
    }

    private fun handleData(qrCredentialJson: String) {
        try {
            if (VerifiableObject(qrCredentialJson).type == VCType.UNKNOWN) {
                throw InvalidCredentialTypeException()
            }

            val aPackage = Package(
                VerifiableObject(qrCredentialJson),
                null, null
            )

            findNavController().navigate(
                R.id.global_action_to_scan_verify, bundleOf(
                    ScanVerifyFragment.KEY_CREDENTIAL_PACKAGE_LIST to listOf(aPackage),
                    ScanVerifyFragment.KEY_IS_CONTACT to false,
                    ScanVerifyFragment.KEY_ORGANIZATION_NAME to "",
                )
            )
        } catch (ex: InvalidCredentialTypeException) {
            activity?.showDialog(
                getString(R.string.unsupported_credential_title),
                getString(R.string.unsupported_credential_body),
                getString(R.string.button_title_ok)
            ) {}
        } catch (ex: Exception) {
            view?.snackShort(getString(R.string.qr_image_invalid))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        qRCodeImageParser.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        qRCodeImageParser.onDestroy()
        _binding = null
    }

    companion object {
        private const val TYPE = "image/*"
    }
}