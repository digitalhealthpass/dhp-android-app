package com.merative.healthpass.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.merative.healthpass.R
import com.merative.healthpass.common.App
import com.merative.healthpass.common.AppConstants.SERVER_ERROR_UNAUTHORIZED_ACCOUNT
import com.merative.healthpass.exception.ExpirationException
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.credentials.isOrganizationCredential
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.baseViews.LoadingDialog
import com.merative.healthpass.ui.mainActivity.FlavorVM
import com.merative.healthpass.ui.organization.OrganizationDetailsFragment
import com.merative.healthpass.ui.organizations_list.OrganizationsListSection
import com.merative.healthpass.ui.results.ScanResultsFragment
import com.merative.healthpass.utils.RxHelper
import com.merative.watson.healthpass.scan.ScanFragment
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import com.merative.watson.healthpass.verifiablecredential.models.credential.Credential
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import retrofit2.HttpException
import java.util.*
import javax.inject.Inject


class HomeFragment : ScanFragment() {
    private lateinit var loadingDialog: LoadingDialog

    @Inject
    protected lateinit var viewModelFactory: ViewModelProvider.Factory

    private val flavorVM: FlavorVM by activityViewModels { viewModelFactory }

    private lateinit var viewModel: HomeViewModel

    private val loadingDisposable = CompositeDisposable()

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().applicationContext as App).appComponent
            .inject(this)

        loadingDialog = LoadingDialog(activity)
        lifecycle.addObserver(loadingDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, viewModelFactory).get(HomeViewModel::class.java)

        setHasOptionsMenu(true)
        handleBack()

        loadCurrentCredential()
    }

    override fun onResume() {
        super.onResume()
        showActionBar()
        applyAppBarLayout { setExpanded(false) }
        lockAppBar(view, true)
        applySupportActionBar {
            setBackgroundDrawable(null)
            setHomeButtonEnabled(false)
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowHomeEnabled(false)
            setHomeActionContentDescription(R.string.button_title_back)
            setHomeAsUpIndicator(null)
        }
    }

    override fun showPermissionDialog() {
        findNavController().navigate(R.id.global_action_to_scanner_permissions_frag)
    }

    private fun handleBack() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    activity?.finishAffinity()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    //region menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    //endregion

    override fun detectQRCode(barCodeString: String?) {
        if (barCodeString != null) {
            stopBarcodeRecognition()
            val credential: Credential? = try {
                parse(barCodeString)
            } catch (ex: Exception) {
                null
            }
            try {
                if (credential?.isOrganizationCredential == true) {
                    navToDetails(barCodeString)
                } else if (flavorVM.selectedCredential == null) {
                    showNoOrganizationCredentialDialog()
                } else if (credential?.isOrganizationCredential != true && flavorVM.selectedCredential != null) {

                    val credential: VerifiableObject? = try {
                        VerifiableObject(barCodeString)
                    } catch (ex: Exception) {
                        null
                    }

                    if (credential == null) {
                        resumeBarcodeRecognition()
                    } else {
                        findNavController().navigate(
                            R.id.action_scanFragment_to_scanResultsFragment, bundleOf(
                                ScanResultsFragment.CREDENTIAL_JSON to barCodeString
                            )
                        )
                    }
                } else {
                    resumeBarcodeRecognition()
                }
            } catch (ex: IllegalArgumentException) {
                resumeBarcodeRecognition()
            }
        }
    }

    private fun showNoOrganizationCredentialDialog() {
        activity?.showDialog(
            getString(R.string.ScanView_no_organization_title),
            getString(R.string.ScanView_no_organization_message),
            getString(R.string.button_title_ok)
        ) { resumeBarcodeRecognition() }
    }

    private fun navToDetails(barCodeString: String) {
        val cp = com.merative.healthpass.models.sharedPref.Package(
            VerifiableObject(barCodeString),
            null, null
        )
        findNavController().navigate(
            R.id.action_Scan_to_org_details, bundleOf(
                OrganizationDetailsFragment.KEY_CREDENTIAL_PACKAGE to cp,
                OrganizationDetailsFragment.KEY_FIRST_TIME to true
            )
        )
    }

    private fun loadCurrentCredential() {
        showLoading(true)
        flavorVM.loadCurrentCredential()
            .doFinally { showLoading(false) }
            .subscribe(
                { showCredentialInformationLabel(it) },
                { handleError(it) }
            )
            .addTo(loadingDisposable)
    }

    private fun handleError(throwable: Throwable) {
        when {
            throwable is ExpirationException ||
                    (throwable is HttpException && throwable.code() == SERVER_ERROR_UNAUTHORIZED_ACCOUNT) -> {
                showInvalidCredentialError()
            }
            else -> handleCommonErrors(throwable)
        }
    }

    private fun showLoading(showLoading: Boolean) {
        loadingDialog.showLoading(showLoading)
    }

    override fun onDestroyView() {
        RxHelper.unsubscribe(loadingDisposable)
        super.onDestroyView()
    }

    private fun showInvalidCredentialError() {
        activity?.showDialog("",
            getString(R.string.ScanView_organization_expired_message2),
            getString(R.string.button_title_ok), {},
            getString(R.string.button_title_cancel), {})
    }

    private fun showCredentialInformationLabel(optional: Optional<Package>) {
        if (!optional.isPresent) {
            if (flavorVM.isAnotherCredentialAvailable) {
                handleCredExpiredWithAvailableCred()
            } else {
                binding.scanOrganization.isVisible = true
                binding.scanOrganization.startBlinkingAnimation()
            }
            return
        }
        val aPackage = optional.get()
        binding.scanOrganization.isVisible = false
        binding.cardViewCredential.let {
            if (aPackage.verifiableObject.credential!!.isExpired()) {
                it.isGone = true

                if (flavorVM.isAnotherCredentialAvailable) {
                    handleCredExpiredWithAvailableCred()
                }
            } else {
                it.isVisible = true
                OrganizationsListSection.adjustOrgCard(it, aPackage, false)

                it.setOnClickListener {
                    findNavController().navigate(
                        R.id.global_action_to_contacts_list_frag
                    )
                }
            }
        }
    }

    private fun handleCredExpiredWithAvailableCred() {
        activity?.showDialog(
            getString(R.string.ScanView_select_organization_title),
            getString(R.string.ScanView_select_organization_message),
            getString(R.string.ScanView_select_organization_button),
            {
                findNavController().navigate(R.id.global_action_to_contacts_list_frag)
            },
            getString(R.string.button_title_cancel),
            {}
        )
    }

    private fun TextView.startBlinkingAnimation() {
        val anim: Animation = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 800
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        this.startAnimation(anim)
    }

    override fun notifyOfflineMode(boolean: Boolean) {
        super.notifyOfflineMode(boolean)
        binding.textviewOffline.visibility = if (boolean) View.GONE else View.VISIBLE
        binding.textviewOffline.setOnClickListener {
            activity?.showDialog(
                getString(R.string.offline_mode),
                getString(R.string.offline_mode_verify_message),
                getString(R.string.button_title_ok),
                {},
                "",
                {}
            )
        }
    }
}