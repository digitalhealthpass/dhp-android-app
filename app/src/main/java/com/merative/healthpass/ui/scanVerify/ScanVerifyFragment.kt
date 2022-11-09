package com.merative.healthpass.ui.scanVerify

import android.os.Bundle
import android.util.Base64
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.merative.healthpass.BuildConfig
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragQrVerifyBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.credential.AsymmetricKey
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import com.merative.healthpass.utils.RxHelper
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import com.merative.watson.healthpass.verifiablecredential.models.VCType
import com.merative.watson.healthpass.verifiablecredential.models.credential.isProfile
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers

open class ScanVerifyFragment : BaseFragment() {

    private var _binding: FragQrVerifyBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var menuAdd: MenuItem? = null

    lateinit var viewModel: ScanVerifyVM
    private val schemaDisposable = SerialDisposable()
    private val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        handleBack()

        _binding = FragQrVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(ScanVerifyVM::class.java)

        viewModel.isContact = arguments?.getBoolean(KEY_IS_CONTACT).orValue(false)
        viewModel.organizationName = arguments?.getString(KEY_ORGANIZATION_NAME).orEmpty()
        viewModel.asymmetricKey = arguments?.get(KEY_ASYMMETRIC_KEY) as? AsymmetricKey

        //parse the cred package list
        viewModel.packageList =
            (arguments?.get(KEY_CREDENTIAL_PACKAGE_LIST) as? List<Package>)
                .orValue(getSchemaEncoded()).toArrayList().orEmpty()

        binding.qrVerifyTxt.apply {
            text = if (viewModel.isContact) {
                getString(R.string.verifying_contact_n_please_wait)
            } else {
                getStringForList(
                    viewModel.packageList,
                    R.string.verifying_credentials_n_please_wait,
                    R.string.verifying_few_credentials_n_please_wait,
                )
            }
        }

        handleBack()
        requestSchema()
    }

    override fun onResume() {
        super.onResume()
        applyAppBarLayout { setExpanded(false) }
        applySupportActionBar {
            setHomeAsUpIndicator(R.drawable.ic_cancel_blue_24dp)
            setHomeActionContentDescription(R.string.button_title_cancel)
        }
    }

    private fun handleBack() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    if (viewModel.isContact) {
                        showDiscardCredentialDialog()
                    } else {
                        goToWallet()
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun getSchemaEncoded(): ArrayList<Package> {
        val list = ArrayList<Package>()
        try {
            val base64Data = arguments?.getString(KEY_QR_CREDENTIAL_JSON_ENCODED)
            if (base64Data.isNotNullOrEmpty()) {
                val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                val aPackage = Package(parse(String(bytes)), null, null)
                if (viewModel.isContact) {
                    aPackage.verifiableObject.type = VCType.CONTACT
                }
                list.add(aPackage)
            }
        } catch (e: IllegalArgumentException) {
            loge("couldn't parse the encoded credential", e, true)

            activity?.showDialog(
                getString(R.string.qr_syntax_invalid),
                getString(R.string.qr_syntax_invalid_title),
                getString(R.string.button_title_ok), {},
                "", {}
            )
        } catch (e: JsonSyntaxException) {
            loge("couldn't parse the encoded credential", e, true)

            activity?.showDialog(
                getString(R.string.qr_syntax_invalid),
                getString(R.string.qr_syntax_invalid_title),
                getString(R.string.button_title_ok), {},
                "", {}
            )
        } catch (e: JsonParseException) {
            loge("couldn't parse the encoded credential", e, true)

            activity?.showDialog(
                getString(R.string.qr_syntax_invalid),
                getString(R.string.qr_syntax_invalid_title),
                getString(R.string.button_title_ok), {},
                "", {}
            )
        }
        return list
    }

    //region requesting schema
    private fun requestSchema() {
        showLoading(true)
        schemaDisposable.set(
            viewModel.requestSchemaAndCheckCredentials()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { showLoading(false) }
                .subscribe({ },
                    {
                        loge(">>> failed schema meta data", it)
                        handleError(it)
                    }, {
                        handleSuccess()
                    })
        )
    }

    private fun handleSuccess() {
        view?.let { initUi(it) }
    }

    private fun handleError(throwable: Throwable) {
        view?.post { goToWallet() }
        if (BuildConfig.DEBUG) {
            handleCommonErrors(throwable)
        } else {
            requireActivity().showDialog(
                getString(R.string.wallet_verification_title),
                getString(R.string.scan_verification_errorMessage),
                getString(R.string.button_title_ok)
            ) { }
        }
    }
    //endregion

    override fun onDestroy() {
        RxHelper.unsubscribe(schemaDisposable.get(), disposables)
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //region menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_scan_verify, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menuAdd = menu.findItem(R.id.add_to_wallet)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_to_wallet -> {
                saveCredential(false)
            }
        }

        return super.onOptionsItemSelected(item)
    }
    //endregion

    private fun saveCredential(forceSave: Boolean) {
        disposables.add(
            viewModel.savePackage(forceSave)
                .subscribe({ saved ->
                    if (!saved) {
                        showCredentialAlreadyExistDialog()
                    } else {
                        if (uploadingAvailable()) {
                            showAddCredentialDialog()
                        } else {
                            goToWallet()
                        }
                    }
                }, rxError("failed to save the data"))
        )
    }

    private fun uploadingAvailable() =
        viewModel.isContact && viewModel.credentialsAvailable && viewModel.contactPackage?.canUpload()
            .orValue(false)

    private fun showAddCredentialDialog() {
        activity?.showDialog(
            getString(R.string.scan_connection_added_title),
            getString(R.string.scan_connection_added_message),
            getString(R.string.scan_connection_added_button_yes),
            { _ -> navigateToCardSelectionScreen() },
            getString(R.string.scan_connection_added_button_no), { goToWallet() }
        )
    }

    private fun navigateToCardSelectionScreen() {
        findNavController().navigate(
            R.id.global_action_credentials_for_upload, bundleOf(
                KEY_CONTACT to viewModel.contactPackage,
                KEY_IS_NEW_CONNECTION to true
            )
        )
    }

    private fun showCredentialAlreadyExistDialog() {
        activity?.showDialog(
            getString(R.string.warning),
            getString(R.string.credential_already_added_message),
            getString(R.string.overwrite),
            { _ ->
                saveCredential(true)
                view?.snackShort(getString(R.string.credential_added_message))
            },
            getString(R.string.button_title_cancel), { goToWallet() }
        )
    }

    /**
     * Pulls the relevant data from the scanned QR code and sets the fields of the UI
     */
    private fun initUi(view: View) {
        binding.qrVerifyTxt.isGone = true
        binding.cardviewCredentialScanComplete.isVisible = !viewModel.isContact
        binding.cardviewContactScanComplete.isVisible = viewModel.isContact

        if (viewModel.packageList.isNotEmpty()) {
            val credential = if (viewModel.isContact) {
                //find the profile credentials, if fails, fall back to the first one
                viewModel.packageList.find { it.verifiableObject.credential!!.isProfile() }
                    .orValue(viewModel.packageList[0])
            } else {
                viewModel.packageList[0]
            }

            val cardView = if (viewModel.isContact) {
                binding.cardviewContactScanComplete
            } else {
                binding.cardviewCredentialScanComplete
            }
            ViewUtils.adjustCredentialView(cardView, credential)

            view.post { menuAdd?.isVisible = true }
        }
    }

    private fun showDiscardCredentialDialog() {
        activity?.showDialog(
            getString(R.string.wallet_discard_title),
            getString(R.string.wallet_discard_message),
            getString(R.string.wallet_discard_yes),
            {
                offBoard()
            },
            getString(R.string.wallet_discard_no),
            {

            })
    }

    private fun offBoard() {
        showLoading(true)
        viewModel.offBoarding()
            .doFinally { showLoading(false) }
            .subscribe({
                if (it.isSuccessfulAndHasBody()) {
                    goToWallet()
                } else {
                    handleCommonErrors(it)
                }
            }, { handleCommonErrors(it) }).addTo(disposables)
    }

    companion object {
        const val KEY_CONTACT = "contact"
        const val KEY_CREDENTIAL_PACKAGE_LIST = "credential_package_list"
        const val KEY_IS_CONTACT = "is_contact"
        const val KEY_ORGANIZATION_NAME = "org_name"
        const val KEY_QR_CREDENTIAL_JSON_ENCODED = "qr_credential_json_encoded"
        const val KEY_QR_CREDENTIAL_DATA = "data"
        const val KEY_ASYMMETRIC_KEY = "asymmetric_key"
        const val KEY_IS_NEW_CONNECTION = "is_new_connection"
    }
}