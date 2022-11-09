package com.merative.healthpass.ui.credential.barcode

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.merative.healthpass.databinding.FragmentFullScreenQrBinding
import com.merative.healthpass.extensions.applyAppBarLayout
import com.merative.healthpass.extensions.setImageFromQR
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.baseViews.BaseFragment

class FullScreenQRFragment : BaseFragment() {

    private var _binding: FragmentFullScreenQrBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFullScreenQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val aPackage = arguments?.get(KEY_CREDENTIAL_PACKAGE) as Package
        binding.fullscreenQrImgView.setImageFromQR(
            aPackage.verifiableObject.rawString,
            Resources.getSystem().displayMetrics.widthPixels
        )
    }

    override fun onResume() {
        super.onResume()
        applyAppBarLayout { setExpanded(false) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_CREDENTIAL_PACKAGE = "package"
    }
}