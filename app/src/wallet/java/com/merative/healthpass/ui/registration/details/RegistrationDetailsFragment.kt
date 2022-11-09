package com.merative.healthpass.ui.registration.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragRegistrationDetailsBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.api.registration.hit.RegistrationPayLoad
import com.merative.healthpass.models.api.registration.nih.DisplaySchemaResponse
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.registration.BaseRegistrationFragment
import com.merative.healthpass.ui.registration.organization.OrganizationFragment
import com.merative.healthpass.ui.registration.registerCode.RegistrationCodeFragment
import com.merative.healthpass.ui.registration.selection.FieldSelectionFragment
import com.merative.healthpass.ui.scanVerify.ScanVerifyFragment
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.schema.Field
import com.merative.healthpass.utils.schema.isArray
import com.merative.watson.healthpass.verifiablecredential.models.credential.toVerifiableObject
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.SerialDisposable

class RegistrationDetailsFragment : BaseRegistrationFragment() {
    private var _binding: FragRegistrationDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    lateinit var viewModel: RegistrationDetailsVM
    private lateinit var regAdapter: RegistrationDetailsAdapter
    private val submitDisposable = SerialDisposable()

    private val validationDisposable = SerialDisposable()
    private val clickDisposable = SerialDisposable()
    private val disposables = CompositeDisposable()

    private val orgName by lazy {
        arguments?.getString(OrganizationFragment.KEY_ORGANIZATION_NAME).orEmpty()
    }
    private val orgCode by lazy {
        arguments?.getString(OrganizationFragment.KEY_REGISTRATION_CODE).orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragRegistrationDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(RegistrationDetailsVM::class.java)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            regAdapter = RegistrationDetailsAdapter(viewModel)
            this.adapter = regAdapter
        }
        if (orgName == ContactPackage.TYPE_NIH) {
            if (viewModel.schema != null) {
                regAdapter.setItems(
                    viewModel.getFieldFromSchema(
                        orgName,
                        orgCode
                    )
                )
            }

            requestSchema()
        } else {
            regAdapter.setItems(
                viewModel.getFieldFromJson(
                    (arguments?.get(RegistrationCodeFragment.KEY_HIT_RESPONSE) as? RegistrationPayLoad)?.registrationForm?.asJsonObject,
                    orgName,
                    orgCode
                )
            )
        }

        validationDisposable.set(
            viewModel.isFormValid
                .subscribe({
                    binding.btnNext.isEnabled = it
                }, rxError("failed to listen to form validation"))
        )
        binding.btnNext.setOnClickListener { submit() }

        clickDisposable.set(
            regAdapter.listenToClickEvents()
                .subscribe({
                    val listToSend = if (it.second.isArray()) {
                        viewModel.fieldsMap[it.second]
                    } else {
                        val list = ArrayList<Any>()
                        if (viewModel.fieldsMap[it.second] != null) {
                            list.add(viewModel.fieldsMap[it.second]!!)
                        }
                        viewModel.fieldsMap[it.second]
                        list
                    }

                    it.first.hideKeyboard()

                    findNavController().navigate(
                        R.id.action_registrationDetailsFragment_to_selectionFragment,
                        bundleOf(
                            FieldSelectionFragment.KEY_FIELD to it.second,
                            FieldSelectionFragment.KEY_SELECTED to listToSend
                        )
                    )

                }, rxError("failed to listen to clicks"))
        )
        listenToSelectionClicks()
    }

    override fun onPause() {
        super.onPause()
        applyDefaultScrollFlags()
    }

    override fun onResume() {
        super.onResume()
        applySupportActionBar {
            setBackgroundDrawable(activity?.getColor(android.R.color.transparent)?.toDrawable())
        }
        removeScrollFlags()
    }

    private fun listenToSelectionClicks() {
        setFragmentResultListener(FieldSelectionFragment.KEY_RESULT) { _, bundle ->
            val key = bundle.get(FieldSelectionFragment.KEY_FIELD) as Field
            val list = bundle.get(FieldSelectionFragment.KEY_SELECTED) as List<Any>

            if (key.isArray()) {
                viewModel.fieldsMap[key] = list
            } else {
                viewModel.fieldsMap[key] = list.firstOrNull()
            }
        }
    }

    //region schema
    private fun requestSchema() {
        val response =
            arguments?.get(RegistrationCodeFragment.KEY_NIH_RESPONSE) as? DisplaySchemaResponse

        if (viewModel.schema?.id != response?.payload) {
            //no need to send the request if it is the same schema, views are being reinitialized
            // when coming back from SelectionFragment
            showLoading(true)
            disposables.add(
                viewModel.requestSchema(response?.payload.orEmpty())
                    .doFinally { showLoading(false) }
                    .subscribe({
                        if (it.isSuccessful) {
                            handleSchemaResponse()
                        } else {
                            handleCommonErrors(it)
                        }
                    }, errorConsumer("failed to get schema response"))
            )
        }
    }

    private fun handleSchemaResponse() {
        regAdapter.setItems(
            viewModel.getFieldFromSchema(
                orgName,
                orgCode
            )
        )
    }
    //endregion

    private fun submit() {
        hideKeyboard()
        showLoading(true)
        submitDisposable.set(
            viewModel
                .submit(
                    arguments?.getString(OrganizationFragment.KEY_ORGANIZATION_NAME).orEmpty()
                ).doFinally { showLoading(false) }
                .subscribe({ response ->
                    val responseBody = response.body()
                    if (response.isSuccessful && responseBody != null) {
                        val bundle = bundleOf(
                            ScanVerifyFragment.KEY_CREDENTIAL_PACKAGE_LIST to responseBody.payload.map {
                                Package(it.toVerifiableObject(), null, null)
                            },
                            ScanVerifyFragment.KEY_IS_CONTACT to true,
                            ScanVerifyFragment.KEY_ORGANIZATION_NAME to arguments?.getString(
                                OrganizationFragment.KEY_ORGANIZATION_NAME
                            ),
                            ScanVerifyFragment.KEY_ASYMMETRIC_KEY to viewModel.asymmetricKey,
                        )

                        findNavController().navigate(R.id.global_action_to_scan_verify, bundle)
                    } else {
                        handleCommonErrors(response)
                    }
                }, errorConsumer("failed to submit the nih flow"))
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        RxHelper.unsubscribe(submitDisposable.get(), validationDisposable.get(), disposables)
        _binding = null
    }
}