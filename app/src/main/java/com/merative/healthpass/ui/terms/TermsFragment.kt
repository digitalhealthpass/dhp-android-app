package com.merative.healthpass.ui.terms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragTermsBinding
import com.merative.healthpass.extensions.applyDefaultScrollFlags
import com.merative.healthpass.extensions.orValue
import com.merative.healthpass.extensions.removeScrollFlags
import com.merative.healthpass.extensions.showTermsDialog
import com.merative.healthpass.ui.common.baseViews.BaseFragment

class TermsFragment : BaseFragment() {
    private var _binding: FragTermsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override val homeBtnEnabled: Boolean
        get() = arguments?.getBoolean(KEY_IS_FROM_SETTINGS).orValue(false)

    lateinit var viewModel: TermsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragTermsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = createViewModel(TermsViewModel::class.java)

        binding.bottomAccept.setOnClickListener {
            viewModel.termsAccepted()
            setFragmentResult(
                KEY_IS_POPPED,
                bundleOf(
                    KEY_FRAG_DATA to true
                )
            )
            findNavController().popBackStack()
        }

        binding.bottomDecline.setOnClickListener {
            showDeclineAlert()
        }

        val isFromSettings = arguments?.getBoolean(KEY_IS_FROM_SETTINGS).orValue(false)
        binding.termsBottomBarLayout.isGone = isFromSettings
        binding.termsTextView.text = getString(TermsConstants.TERMS_ID)
    }

    private fun showDeclineAlert() {
        activity?.showTermsDialog(
            getString(R.string.t_c_title),
            getString(R.string.t_c_message),
            getString(R.string.t_c_continue),
            {},
            getString(R.string.t_c_disagree),
            {
                viewModel.termsDeclined()
                setFragmentResult(
                    KEY_IS_POPPED,
                    bundleOf(
                        KEY_FRAG_DATA to false
                    )
                )
                findNavController().popBackStack()
            }
        )
    }

    override fun onResume() {
        super.onResume()
        removeScrollFlags()
    }

    override fun onPause() {
        super.onPause()
        applyDefaultScrollFlags()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_IS_FROM_SETTINGS = "from_settings"
    }
}