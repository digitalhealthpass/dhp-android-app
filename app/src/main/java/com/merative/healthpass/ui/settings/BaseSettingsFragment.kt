package com.merative.healthpass.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.clearFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.merative.healthpass.BuildConfig
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragSettingsBinding
import com.merative.healthpass.extensions.buildVersion
import com.merative.healthpass.extensions.navigateSafe
import com.merative.healthpass.extensions.showDebugDialog
import com.merative.healthpass.models.region.getEnvironmentTitle
import com.merative.healthpass.ui.common.baseViews.BaseBottomSheet
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.privacy.PrivacyPolicyFragment
import com.merative.healthpass.ui.terms.TermsFragment
import com.merative.healthpass.ui.thirdParty.ThirdPartyNoticeFragment
import java.util.*

abstract class BaseSettingsFragment : BaseFragment() {

    private var _binding: FragSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    protected val binding get() = _binding!!

    lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(SettingsViewModel::class.java)
        setupBaseUI(view)
        setupTopUI(view)
    }

    protected abstract fun setupTopUI(root: View)

    private fun setupBaseUI(root: View) {

        binding.buttonReset.setOnClickListener {
            navigateSafe(R.id.action_settings_to_resetCache)
        }

        binding.buttonProfileTerms.setOnClickListener {
            navigateSafe(
                R.id.global_action_to_terms_frag, bundleOf(
                    TermsFragment.KEY_IS_FROM_SETTINGS to true
                )
            )
        }

        binding.buttonProfilePrivacy.setOnClickListener {
            navigateSafe(
                R.id.global_action_to_privacy_frag,
                bundleOf(
                    PrivacyPolicyFragment.KEY_IS_FROM_SETTINGS to true
                )
            )
        }

        binding.buttonThirdParty.setOnClickListener {
            navigateSafe(
                R.id.global_action_to_third_party_frag,
                bundleOf(
                    ThirdPartyNoticeFragment.KEY_IS_FROM_SETTINGS to true
                )
            )
        }

        binding.textViewProfileBuildVersion.apply {
            binding.right.text = buildVersion

            if (BuildConfig.DEBUG) {
                setOnClickListener {
                    activity?.showDebugDialog()
                }
            }
        }

        binding.textViewProfileEnv.apply {
            binding.right.text = viewModel.getEnvironment().getEnvironmentTitle(resources)
            setOnClickListener {
                setFragmentResultListener(BaseBottomSheet.KEY_IS_DISMISSED) { _, bundle ->
                    if (bundle.getBoolean(BaseBottomSheet.KEY_RESULT_DATA)) {
                        clearFragmentResult(BaseBottomSheet.KEY_IS_DISMISSED)
                        binding.right.text =
                            viewModel.getEnvironment().getEnvironmentTitle(resources)

                    }
                }
                navigateSafe(R.id.global_action_to_region_selection_frag)
            }
        }

        binding.textViewProfileLanguage.apply {
            binding.right.text = getLanguage()
        }
    }

    private fun getLanguage(): String {
        var language = Locale.getDefault().language
        return if (language == Locale.GERMAN.language || language == Locale.ENGLISH.language) {
            language
        } else {
            Locale.ENGLISH.language
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
