package com.merative.healthpass.ui.debug.debugFragment

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.bold
import androidx.core.text.color
import androidx.lifecycle.ViewModelProvider
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.merative.healthpass.BuildConfig
import com.merative.healthpass.R
import com.merative.healthpass.common.App
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.extensions.*
import com.merative.healthpass.network.interceptors.HeadersInterceptor
import java.util.*
import javax.inject.Inject

class DebugFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: DebugVM

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        (requireActivity().applicationContext as App).appComponent
            .inject(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(DebugVM::class.java)

        setPreferencesFromResource(R.xml.debug_preference, rootKey)
        handleFirstTimeExperience()
        handleAppInfo()
        handleRootCheck()
    }

    private fun handleFirstTimeExperience() {
        findPreference<Preference>(getString(R.string.key_reset_fti))?.apply {
            setOnPreferenceClickListener {
                viewModel.sharedPrefUtils.resetFirstTimeExperience()
                activity?.restartApplication()
                true
            }
        }
    }

    private fun handleAppInfo() {
        findPreference<Preference>(getString(R.string.key_debug_build_info))?.apply {
            val sbb = SpannableStringBuilder()
            sbb.bold { append("App ID: ") }.append(BuildConfig.APPLICATION_ID).appendLine()
            sbb.bold { append("Build v: ") }.append(buildVersion).appendLine()
            sbb.bold { append("Env: ") }.append(viewModel.environmentHandler.currentEnv.title)
                .appendLine()
            sbb.bold { append("URL: ") }.append(viewModel.environmentHandler.currentEnv.url)
                .appendLine()
            sbb.bold { append("Build Type: ") }.append(BuildConfig.BUILD_TYPE).appendLine()

            sbb.bold { append("Has localization: ") }.append(hasLocalization())
                .appendLine()

            sbb.bold { appendLine("Last ${HeadersInterceptor.MAX_SIZE} requests txn-id: ") }
                .append(HeadersInterceptor.getLast5UUID().trim())
            summary = sbb
        }
    }

    private fun hasLocalization(): CharSequence {
        val currentResources = resources
        val config = Configuration(currentResources.configuration)
        config.setLocale(Locale.GERMAN)
        val hasLocalization = try {
            //this may need maintenance from the Dev team, because of the value comparison
            val okString =
                activity?.createConfigurationContext(config)?.resources
                    ?.getString(R.string.button_title_ok)
                    .orValue("ok")

            okString.toLowerCase() != "ok"
        } catch (ex: Exception) {
            loge("couldn't find the string", ex)
            false
        }
        val color = if (hasLocalization) Color.GREEN else Color.RED

        return SpannableStringBuilder().color(color) {
            append(hasLocalization.toString())
        }
    }

    private fun handleRootCheck() {
        findPreference<SwitchPreference>(AppConstants.KEY_PREF_ROOT_CHECK)?.apply {
        }
    }
}