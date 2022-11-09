package com.merative.healthpass.ui.contactDetails.revoke

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.merative.healthpass.databinding.FragConsentRevokeBinding
import com.merative.healthpass.extensions.handleCommonErrors
import com.merative.healthpass.extensions.isSuccessfulAndHasBody
import com.merative.healthpass.extensions.showDialog
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.utils.RxHelper
import io.reactivex.rxjava3.disposables.SerialDisposable


class ConsentRevokeFragment : BaseFragment() {

    private var _binding: FragConsentRevokeBinding? = null
    private val binding get() = _binding!!

    lateinit var viewModel: ConsentRevokeVM
    private val consentAndSubmitDisposable = SerialDisposable()
    private val deleteDisposable = SerialDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragConsentRevokeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(ConsentRevokeVM::class.java)

        binding.consentRevokeBtnDelete.setOnClickListener { submit() }
    }

    private fun submit() {
        showLoading(true)
        consentAndSubmitDisposable.set(
            viewModel.requestConsentReceiptAndSubmit(
                arguments?.get(KEY_CONTACT) as ContactPackage,
                binding.consentRevokeSwitchRevoke.isChecked
            ).doFinally { showLoading(false) }
                .subscribe({
                    if (it.isSuccessfulAndHasBody()) {
                        //navigate some where
                        goToWallet()
                    } else {
                        handleCommonErrors(it)
                        deleteAnyway()
                    }
                }, {
                    it.printStackTrace()
                    deleteAnyway()
                })
        )
    }

    private fun deleteAnyway() {
        activity?.showDialog(
            "Off-boarding Incomplete",
            "Couldn't complete the off-boarding process with the contact. Do you still want to remove this contact from Wallet?",
            "Yes, Remove",
            {
                deleteDisposable.set(
                    viewModel.deleteContactFromDB(arguments?.get(KEY_CONTACT) as ContactPackage)
                        .subscribe {
                            goToWallet()
                        }
                )
            },
            "Cancel",
            {}
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        RxHelper.unsubscribe(consentAndSubmitDisposable.get(), deleteDisposable.get())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_CONTACT = "contact"
    }
}