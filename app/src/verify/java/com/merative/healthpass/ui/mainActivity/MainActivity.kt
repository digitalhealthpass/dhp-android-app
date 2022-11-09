package com.merative.healthpass.ui.mainActivity

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.gson.JsonSyntaxException
import com.merative.healthpass.R
import com.merative.healthpass.databinding.ActivityMainBinding
import com.merative.healthpass.extensions.contentToString
import com.merative.healthpass.extensions.getContentName
import com.merative.healthpass.extensions.logd
import com.merative.healthpass.extensions.loge
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.baseViews.BaseActivity
import com.merative.healthpass.ui.scanVerify.ScanVerifyFragment
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import com.merative.watson.healthpass.verifiablecredential.extensions.toJSONObject
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject

class MainActivity : BaseActivity() {

    lateinit var appBarLayout: AppBarLayout
    lateinit var binding: ActivityMainBinding
        private set

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var appBarConfiguration: AppBarConfiguration

    protected val mainActivityVM: MainActivityVM by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        adjustNavController()
    }

    private fun adjustNavController() {
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.findNavController()

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            navGraph = navController.graph,
            fallbackOnNavigateUpListener = {
                false
            }
        )

        appBarLayout = binding.appBarLayout
        val layout = findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar_layout)
        setSupportActionBar(binding.toolbar)

        layout.setupWithNavController(binding.toolbar, navController, appBarConfiguration)
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

//        toolbar.setupWithNavController(navController, appBarConfiguration)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//        return navController.popBackStack()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        passIntentToNavHost(intent)
    }

    private fun passIntentToNavHost(intent: Intent?) {
        val data = intent?.data ?: return
        if (isSharedFile(intent)) {
            handleCredentialFile(intent)
        }
        if (!mainActivityVM.isPinValidationRequired) {
            try {
                findNavController(R.id.nav_host_fragment).navigate(data)
            } catch (ex: IllegalArgumentException) {
                loge("couldn't navigate to deep link", ex, true)
            }
        }
    }

    override fun onSaveInstanceState(oldInstanceState: Bundle) {
        super.onSaveInstanceState(oldInstanceState)
        oldInstanceState.clear()
    }

    fun isSharedFile(intent: Intent?): Boolean {
        if (intent == null) {
            return false
        }
        val scheme: String = intent.scheme.orEmpty()
        val action: String = intent.action.orEmpty()

        if (action.compareTo(Intent.ACTION_VIEW) == 0 && intent.data != null) {

            if (scheme == ContentResolver.SCHEME_CONTENT) {
                val uri: Uri = intent.data!!
                val resolver: ContentResolver = contentResolver
                val name = resolver.getContentName(uri)
                if (name?.contains(".w3c") == false && !name.contains(".smart-health-card")) {
                    return false
                }
                val type = resolver.getType(uri)
                logd(
                    "Content intent detected: $action ${intent.dataString} : ${intent.type} : $name, and resolver type: $type"
                )
                val inputStream = resolver.openInputStream(uri)

                val json = inputStream?.contentToString().orEmpty()
                logd("---- json:$json")
                return try {
                    //try to parse to confirm it is valid json
                    val jsonObject = json.toJSONObject()
                    if (jsonObject?.has("verifiableCredential") == true) {
                        VerifiableObject(
                            jsonObject.getJSONArray("verifiableCredential").getString(0)
                        )
                    } else {
                        VerifiableObject(json)
                    }
                    true
                } catch (ex: JsonSyntaxException) {
                    loge("failed to open cred file", ex)
                    false
                }
            }
        }
        return false
    }

    fun handleCredentialFile(intent: Intent) {
        val scheme: String = intent.scheme.orEmpty()
        val action: String = intent.action.orEmpty()

        if (action.compareTo(Intent.ACTION_VIEW) == 0 && intent.data != null) {

            if (scheme == ContentResolver.SCHEME_CONTENT) {
                val uri: Uri = intent.data!!
                val resolver: ContentResolver = contentResolver
                val inputStream = resolver.openInputStream(uri)

                val json = inputStream?.contentToString().orEmpty()
                val jsonObject = json.toJSONObject()

                val packages = if (jsonObject?.has("verifiableCredential") == true) {
                    listOf(
                        Package(
                            parse(
                                jsonObject.getJSONArray("verifiableCredential").getString(0)
                            ), null, null
                        )
                    )
                } else {
                    listOf(Package(parse(json), null, null))
                }

                findNavController(R.id.nav_host_fragment).navigate(
                    R.id.global_action_to_scan_verify,
                    bundleOf(
                        ScanVerifyFragment.KEY_IS_CONTACT to false,
                        ScanVerifyFragment.KEY_CREDENTIAL_PACKAGE_LIST to packages,
                        ScanVerifyFragment.KEY_ORGANIZATION_NAME to "",
                    )
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // nav_host_fragment.findNavController().navigate(R.id.action_wallet_to_settings)

//                    return true
                // } else {
//                    onBackPressed()
                // }
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
                || item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment))
    }

    private fun getCurrentFragment(): Fragment? {
        val navHostFragment: NavHostFragment? =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount != 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            return super.onBackPressed()
        }
    }
}
