package com.merative.healthpass.ui.privacy

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.FORCE_DARK_OFF
import androidx.webkit.WebSettingsCompat.FORCE_DARK_ON
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewFeature
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragPrivacyPolicyBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.utils.TimeoutWebViewClient
import java.io.BufferedReader


class PrivacyPolicyFragment : BaseFragment() {
    private var _binding: FragPrivacyPolicyBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    lateinit var viewModel: PrivacyPolicyVM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        _binding = FragPrivacyPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(PrivacyPolicyVM::class.java)

        initWebView()
    }

    override fun enableBackButton(finishOnBackPressed: Boolean) {
        var drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_clear_24, null)
        drawable = DrawableCompat.wrap(drawable!!)
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this.requireContext(), R.color.textLabels))

        applySupportActionBar {
            setBackgroundDrawable(null)
            setHomeButtonEnabled(homeBtnEnabled)
            setDisplayHomeAsUpEnabled(homeBtnEnabled)
            setDisplayShowHomeEnabled(homeBtnEnabled)
            setHomeActionContentDescription(R.string.button_title_back)
            if (homeBtnEnabled) {
                setHomeAsUpIndicator(drawable)
            }
            if (finishOnBackPressed) {
                addBackDispatcher()
            }
        }
    }

    private fun initWebView() {
        enableDarkMode()
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.minimumFontSize = resources.getInteger(R.integer.webView_small_font_size)
        binding.webView.settings.javaScriptCanOpenWindowsAutomatically = true
        val text = activity?.let { readAsset(it, FILE_NAME_PRIVACY_POLICY) }
        val assetLoader = activity?.let { WebViewAssetLoader.ResourcesPathHandler(it) }?.let {
            WebViewAssetLoader.Builder()
                .addPathHandler(WEBVIEW_RES_PATH_HANDLER, it)
                .build()
        }

        binding.webView.webViewClient = object : TimeoutWebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView, request: WebResourceRequest,
            ): WebResourceResponse? {
                return assetLoader?.shouldInterceptRequest(request.url)
            }
        }

        if (text != null) {
            binding.webView.loadDataWithBaseURL(WEBVIEW_BASE_URL, text, MIME_TYPE, ENCODING, null)
        }

        binding.webView.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    val webView = v as WebView
                    when (keyCode) {
                        KeyEvent.KEYCODE_BACK -> if (webView.canGoBack()) {
                            webView.goBack()
                            return true
                        }
                    }
                }
                return false
            }
        })
    }

    private fun enableDarkMode() {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    WebSettingsCompat.setForceDark(binding.webView.settings, FORCE_DARK_ON)
                }
                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    WebSettingsCompat.setForceDark(binding.webView.settings, FORCE_DARK_OFF)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyAppBarLayout {
            setExpanded(false)
        }
        lockAppBar(view, false)
    }

    override fun onPause() {
        super.onPause()
        applyDefaultScrollFlags()
    }

    override fun onDestroy() {
        showLoading(false)
        viewModel.privacyAccepted()
        //check if initial flow, to automate flow for new users
        if (arguments?.getBoolean(KEY_IS_INITIAL_FLOW).orValue(false)) {
            setFragmentResult(
                KEY_IS_POPPED,
                bundleOf(
                    KEY_FRAG_DATA to true
                )
            )
        }
        super.onDestroy()
    }

    private fun readAsset(context: Context, fileName: String): String =
        context
            .assets
            .open(fileName)
            .bufferedReader()
            .use(BufferedReader::readText)

    companion object {
        const val KEY_IS_FROM_SETTINGS = "from_settings"
        const val KEY_IS_INITIAL_FLOW = "initial_flow"
        const val MIME_TYPE = "text/html"
        const val ENCODING = "UTF-8"
        const val WEBVIEW_RES_PATH_HANDLER = "/res/"
        const val WEBVIEW_BASE_URL = "https://appassets.androidplatform.net"
        const val FILE_NAME_PRIVACY_POLICY = "privacy_policy.html"
    }
}