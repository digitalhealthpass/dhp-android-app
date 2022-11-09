package com.merative.healthpass.ui.scanner_permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragScannerPermissionsBinding
import com.merative.healthpass.extensions.applyAppBarLayout
import com.merative.healthpass.extensions.goToSettings
import com.merative.healthpass.ui.common.baseViews.BaseBottomSheet
import com.merative.healthpass.ui.common.baseViews.BaseFragment

class ScannerPermissionsFragment : BaseFragment() {

    private var _binding: FragScannerPermissionsBinding? = null
    private val binding get() = _binding!!

    protected lateinit var viewModel: ScannerPermissionsVM

    private val requestCameraPermissionCode = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragScannerPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(ScannerPermissionsVM::class.java)

        binding.permissionsSettings.apply {
            if (viewModel.isPermissionDenied()) {
                text = getString(R.string.settings_title)
                setOnClickListener {
                    activity?.goToSettings()
                    findNavController().popBackStack()
                }
            } else {
                setOnClickListener {
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        requestCameraPermissionCode
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == requestCameraPermissionCode && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setFragmentResult(
                    BaseBottomSheet.KEY_IS_DISMISSED,
                    bundleOf(
                        BaseBottomSheet.KEY_RESULT_DATA to true
                    )
                )
                findNavController().popBackStack()
            } else {
                viewModel.onPermissionDenied()
                setFragmentResult(
                    BaseBottomSheet.KEY_IS_DISMISSED,
                    bundleOf(
                        BaseBottomSheet.KEY_RESULT_DATA to false
                    )
                )
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        setFragmentResult(
            KEY_IS_POPPED,
            bundleOf(
                KEY_FRAG_DATA to false
            )
        )
    }

    override fun onResume() {
        super.onResume()
        applyAppBarLayout {
            setExpanded(false)
        }
    }
}