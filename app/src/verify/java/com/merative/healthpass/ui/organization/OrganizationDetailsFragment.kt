package com.merative.healthpass.ui.organization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.merative.healthpass.R
import com.merative.healthpass.common.AppConstants
import com.merative.healthpass.databinding.FragOrganizationDetailsBinding
import com.merative.healthpass.exception.ExpirationException
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.OfflineModeException
import com.merative.healthpass.models.OrganizationException
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.schema.SchemaProcessor
import com.merative.watson.healthpass.scan.ScanFragment
import com.merative.watson.healthpass.verifiablecredential.models.credential.credentialSubjectType
import com.merative.watson.healthpass.verifiablecredential.models.credential.getOrgName
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.SerialDisposable
import retrofit2.HttpException

class OrganizationDetailsFragment : BaseFragment() {
    private var _binding: FragOrganizationDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: OrganizationDetailsVM
    private lateinit var aPackage: Package
    private val deleteDisposable = SerialDisposable()
    private val loadingDisposable = SerialDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aPackage = requireArguments().get(KEY_CREDENTIAL_PACKAGE) as Package
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragOrganizationDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(OrganizationDetailsVM::class.java)

        initUI()
        applyAppBarLayout {
            setExpanded(false)
        }
        lockAppBar(view, false)
    }

    private fun initUI() {
        val credential = aPackage.verifiableObject.credential!!

        binding.orgImageview.setImageFromBase64(
            aPackage.issuerMetaData?.metadata?.logo,
            credential.getOrgName()?.getInitials()
        )
        binding.orgNameHeader.text = credential.getOrgName()
        binding.orgTypeSubheader.text = credential.credentialSubjectType
        binding.orgIssueDateTv.text =
            getString(R.string.result_issuedFormat, credential.getFormattedIssuedDate())
        val expDateString = credential.getFormattedExpiryDate()
        val prefixStr = if (credential.isExpired()) R.string.result_expiredDate else R.string.result_expiresDate
        binding.orgExpDateTv.text = getString(prefixStr) + " $expDateString"

        val fieldsList = SchemaProcessor().processSchemaAndSubject(credential, aPackage.schema)

        val filteredList = fieldsList.filter { field -> field.visible }.toArrayList()
        binding.orgDetailsRv.layoutManager = LinearLayoutManager(requireContext())
        val adapter = OrgDetailsAdapter()
        adapter.setItems(filteredList)
        binding.orgDetailsRv.adapter = adapter

        binding.orgBeginScanButton.isGone = credential.isExpired()
        binding.orgBeginScanButton.text = if (flavorVM.selectedCredential == aPackage) {
            getString(R.string.OrganizationDetails_organization_selection_button2)
        } else {
            getString(R.string.OrganizationDetails_organization_selection_button1)
        }

        binding.orgBeginScanButton.setOnClickListener {
            loginAndSave()
        }

        binding.orgTxtExpired.isVisible = credential.isExpired()

        binding.orgDiscardButton.setOnClickListener {
            activity?.showDialog(
                getString(R.string.OrganizationDetails_discard_organization_title),
                getString(R.string.OrganizationDetails_discard_organization_message),
                getString(R.string.wallet_discard_yes),
                {
                    deleteOrg()
                },
                getString(R.string.wallet_discard_no),
                {}
            )
        }
    }

    private fun loginAndSave() {
        val isFirstTime = arguments?.getBoolean(KEY_FIRST_TIME).orValue(false)
        showLoading(true)
        loadingDisposable.set(
            viewModel.loginWithOrgCredential(aPackage, flavorVM.selectedCredential, isFirstTime)
                .doFinally { showLoading(false) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { handleLoginSuccess(it) },
                    { handleLoginError(it) }
                )
        )
    }

    private fun handleLoginSuccess(updatedCP: Package?) {
        flavorVM.selectedCredential = updatedCP
        navigateToCameraScreen()
    }

    private fun handleLoginError(throwable: Throwable) =
        when {
            throwable is ExpirationException -> showExpiredCredentialError()
            throwable is OfflineModeException -> showOfflineModeError()
            isCredentialInvalid(throwable) -> showInvalidCredentialError()
            else -> handleCommonErrors(throwable)
        }

    private fun isCredentialInvalid(throwable: Throwable) =
        ((throwable is HttpException && throwable.code() == AppConstants.SERVER_ERROR_UNAUTHORIZED_ACCOUNT)
                || (throwable is OrganizationException))

    private fun showExpiredCredentialError() {
        activity?.showDialog("",
            getString(R.string.ScanView_organization_expired_message1),
            getString(R.string.button_title_ok), { navigateToCameraScreen() },
            getString(R.string.button_title_cancel), {})
    }

    private fun showInvalidCredentialError() {
        activity?.showDialog(getString(R.string.authentication_failed_title),
            getString(R.string.authentication_failed_message),
            getString(R.string.button_title_ok), { navigateToCameraScreen() },
            getString(R.string.button_title_cancel), {})
    }

    private fun showOfflineModeError() {
        // ignore
    }

    private fun navigateToCameraScreen() {
        findNavController().navigate(
            R.id.global_action_pop_to_home,
            bundleOf(
                ScanFragment.KEY_SHOW_TORCH to true,
                ScanFragment.KEY_TOGGLE_CAMERA to true
            )
        )
    }

    private fun deleteOrg() {
        showLoading(true)
        deleteDisposable.set(
            viewModel.deleteOrganization(aPackage)
                .doFinally { showLoading(false) }
                .subscribe(
                    {
                        flavorVM.credentialDeleted(aPackage)
                        findNavController().popBackStack()
                    },
                    {
                        handleCommonErrors(it)
                    }
                )
        )
    }

    override fun onDestroyView() {
        RxHelper.unsubscribe(deleteDisposable.get(), loadingDisposable.get())
        lockAppBar(view, true)
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_CREDENTIAL_PACKAGE = "package"
        const val KEY_FIRST_TIME = "isFirstTime"
    }
}