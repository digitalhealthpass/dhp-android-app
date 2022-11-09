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
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.baseViews.BaseActivity
import com.merative.healthpass.ui.common.baseViews.LoadingDialog
import com.merative.healthpass.ui.contactDetails.details.ContactDetailsFragment
import com.merative.healthpass.ui.home.HomeFragment
import com.merative.healthpass.ui.registration.organization.OrganizationFragment
import com.merative.healthpass.ui.scanVerify.ScanVerifyFragment
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import com.merative.watson.healthpass.verifiablecredential.extensions.toJSONObject
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import io.reactivex.rxjava3.disposables.SerialDisposable

class MainActivity : BaseActivity() {

    lateinit var appBarLayout: AppBarLayout
    lateinit var binding: ActivityMainBinding

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var loadingDialog: LoadingDialog? = null
    private val contactDisposable = SerialDisposable()

    protected val mainActivityVM: MainActivityVM by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loadingDialog = LoadingDialog(this)
        loadingDialog?.let(lifecycle::addObserver)
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
        setIntent(intent)
        passIntentToNavHost(intent)
    }

    private fun passIntentToNavHost(intent: Intent?) {
        intent?.data ?: return
        if (isSharedFile(intent)) {
            handleCredentialFile(intent)
        }
        /*if (!mainActivityVM.isPinValidationRequired) {
            try {
                findNavController(R.id.nav_host_fragment).navigate(data)
            } catch (ex: IllegalArgumentException) {
                loge("couldn't navigate to deep link", ex, true)
            }
        }*/
        if (hasDeepLink() && !mainActivityVM.shouldDisableFeatureForRegion()) {
            handledDeepLink()
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

    private fun hasDeepLink(): Boolean {
        return intent?.data?.getQueryParameter(HomeFragment.KEY_CONTACT_CRED_ID)
            .isNotNullOrEmpty()
                || intent?.data?.getQueryParameter(OrganizationFragment.KEY_ORGANIZATION_NAME)
            .isNotNullOrEmpty()
                || intent?.data?.getQueryParameter(ScanVerifyFragment.KEY_QR_CREDENTIAL_DATA)
            .isNotNullOrEmpty()
                || this.isSharedFile(intent)
    }

     fun handledDeepLink() {
        when {
            intent?.data?.getQueryParameter(HomeFragment.KEY_CONTACT_CRED_ID)
                .isNotNullOrEmpty() -> {
                handleDownloadingContactCred(intent)
            }
            intent?.data?.getQueryParameter(OrganizationFragment.KEY_ORGANIZATION_NAME)
                .isNotNullOrEmpty() -> {
                handleRegisteringOrg(intent)
            }
            intent?.data?.getQueryParameter(ScanVerifyFragment.KEY_QR_CREDENTIAL_DATA)
                .isNotNullOrEmpty() -> {
                handleEncodedCredential(intent)
            }
            this.isSharedFile(intent) -> {
                this.handleCredentialFile(intent!!)
            }
        }
    }

    protected fun showLoading(show: Boolean) {
        loadingDialog?.showLoading(show)
    }

    private fun handleDownloadingContactCred(intent: Intent?) {
        if (intent?.data?.getQueryParameter(HomeFragment.KEY_CONTACT_CRED_ID).isNotNullOrEmpty()) {
            showLoading(true)
            contactDisposable.set(
                mainActivityVM.loadContact(
                    intent?.data?.getQueryParameter(
                        HomeFragment.KEY_CONTACT_CRED_ID
                    ).orEmpty()
                )
                    .doFinally { showLoading(false) }
                    .subscribe({ contactPackage ->
                        intent?.removeExtra(HomeFragment.KEY_CONTACT_CRED_ID)
                        goToWallet()
                        findNavController(R.id.nav_host_fragment).navigate(
                            R.id.global_action_contact_details,
                            bundleOf(
                                ContactDetailsFragment.KEY_CONTACT_PACKAGE to contactPackage,
                                ContactDetailsFragment.KEY_DOWNLOAD to true
                            )
                        )
                    }, {
                        intent?.removeExtra(HomeFragment.KEY_CONTACT_CRED_ID)
                        it.printStackTrace()
                        showDialog(
                            null,
                            getString(R.string.no_contact_for_credentials),
                            getString(R.string.button_title_ok)
                        ) {
                            goToWallet()
                        }
                    }, {
                        intent?.removeExtra(HomeFragment.KEY_CONTACT_CRED_ID)
                        showDialog(
                            null,
                            getString(R.string.no_contact_for_credentials),
                            getString(R.string.button_title_ok)
                        ) {
                            goToWallet()
                        }
                    })
            )
        }
    }

    private fun handleRegisteringOrg(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.getQueryParameter(OrganizationFragment.KEY_ORGANIZATION_NAME)
                .isNotNullOrEmpty()
        ) {
            goToWallet()
            findNavController(R.id.nav_host_fragment).navigate(
                R.id.registration_navigation,
                bundleOf(OrganizationFragment.KEY_ORGANIZATION_NAME to data.getQueryParameter(OrganizationFragment.KEY_ORGANIZATION_NAME),
                    OrganizationFragment.KEY_REGISTRATION_CODE to data.getQueryParameter(OrganizationFragment.KEY_REGISTRATION_CODE))
            )
        }
    }

    private fun handleEncodedCredential(intent: Intent?) {
        if (intent?.data?.getQueryParameter(ScanVerifyFragment.KEY_QR_CREDENTIAL_DATA)
                .isNotNullOrEmpty()
        ) {
            goToWallet()
            findNavController(R.id.nav_host_fragment).navigate(
                R.id.global_action_to_scan_verify,
                bundleOf(ScanVerifyFragment.KEY_QR_CREDENTIAL_JSON_ENCODED to intent?.data?.getQueryParameter(ScanVerifyFragment.KEY_QR_CREDENTIAL_DATA))
            )
        }
    }

    protected fun goToWallet() {
        findNavController(R.id.nav_host_fragment).navigate(R.id.global_action_pop_to_home)
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingDialog = null
    }
}