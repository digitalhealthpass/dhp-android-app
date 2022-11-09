package com.merative.healthpass.ui.contactDetails.availableCred

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragCredentialsBinding
import com.merative.healthpass.extensions.rxError
import com.merative.healthpass.extensions.showDialog
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.contactDetails.consent.ConsentFragment
import com.merative.healthpass.ui.contactDetails.shareCred.ShareCredentialsFragment
import com.merative.healthpass.ui.scanVerify.ScanVerifyFragment
import com.merative.healthpass.utils.RxHelper
import io.reactivex.rxjava3.disposables.CompositeDisposable

class SelectCredentialsFragment : BaseFragment() {

    private var _binding: FragCredentialsBinding? = null
    private val binding get() = _binding!!

    private var menuNext: MenuItem? = null

    lateinit var viewModel: SelectCredentialsVM
    private val availableCredAdapter = SelectCredentialsAdapter(true)
    private val unavailableCredAdapter = SelectCredentialsAdapter(true)
    private val unsupportedCredAdapter = SelectCredentialsAdapter(false)
    private val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        if (arguments?.get(ScanVerifyFragment.KEY_IS_NEW_CONNECTION) as? Boolean == true) {
            handleBack()
        }
        setHasOptionsMenu(true)

        _binding = FragCredentialsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(SelectCredentialsVM::class.java)

        val contact = arguments?.get(ConsentFragment.KEY_CONTACT) as? ContactPackage

        binding.recyclerViewAvailableCredentials.apply {
            layoutManager = LinearLayoutManager(activity)
            availableCredAdapter.selectedPackages = viewModel.selectedCredentials
            this.adapter = availableCredAdapter
        }

        binding.recyclerViewUnavailableCredentials.apply {
            layoutManager = LinearLayoutManager(activity)
            unavailableCredAdapter.selectedPackages = viewModel.selectedCredentials
            this.adapter = unavailableCredAdapter
        }

        binding.recyclerViewUnsupportedCredentials.apply {
            layoutManager = LinearLayoutManager(activity)
            unsupportedCredAdapter.selectedPackages = viewModel.selectedCredentials
            this.adapter = unsupportedCredAdapter
        }

        disposables.add(
            viewModel.loadAllCredentials(contact)
                .subscribe({
                    val available = it.availableList
                    val unavailable = it.unavailableList
                    val unsupported = it.unsupportedList

                    availableCredAdapter.setItems(available)
                    unavailableCredAdapter.setItems(unavailable)
                    unsupportedCredAdapter.setItems(unsupported)

                    binding.fragCredentialsTxtAvailable.isVisible = available.isNotEmpty()
                    binding.recyclerViewAvailableCredentials.isVisible = available.isNotEmpty()

                    binding.fragCredentialsTxtUnavailable.isVisible = unavailable.isNotEmpty()
                    binding.recyclerViewUnavailableCredentials.isVisible = unavailable.isNotEmpty()

                    binding.fragCredentialsTxtUnsupported.isVisible = unsupported.isNotEmpty()
                    binding.recyclerViewUnsupportedCredentials.isVisible = unsupported.isNotEmpty()
                }, rxError("couldn't load the credentials"))
        )

        disposables.add(
            availableCredAdapter.listenToClickEvents()
                .mergeWith(unavailableCredAdapter.listenToClickEvents())
                .subscribe({
                    viewModel.addOrRemoveCredentials(it.second)
                }, rxError("failed to listen to clicks"))
        )
//        disposables.add(
//            unavailableCredAdapter.listenToClickEvents()
//                .subscribe({
//                    viewModel.addOrRemoveCredentials(it.second)
//                }, rxError("failed to listen to clicks"))
//        )
    }

    //region menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_credentials, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menuNext = menu.findItem(R.id.menu_next)
        //menu takes more time to be created comparing to onViewCreated
        disposables.add(
            viewModel.isListEmpty
                .subscribe({ isEmpty ->
                    menuNext?.isVisible = !isEmpty
                }, rxError(""))
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_next -> {
                confirmOrNavigateAction()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //endregion

    private fun confirmOrNavigateAction() {
        if (viewModel.containsUploadedCards) {
            activity?.showDialog(
                getString(R.string.contact_credentials_reupload_title),
                getString(R.string.contact_credentials_reupload_message),
                getString(R.string.contact_credentials_reupload_yes),
                { navigateToConfirmAndShare() },
                getString(R.string.button_title_cancel),
                {})
        } else {
            navigateToConfirmAndShare()
        }
    }

    private fun handleBack() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    goToWallet()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun navigateToConfirmAndShare() {
        findNavController().navigate(
            R.id.action_credentialsFragment_to_shareFragment,
            bundleOf(
                ShareCredentialsFragment.KEY_CONTACT to arguments?.get(ShareCredentialsFragment.KEY_CONTACT) as? ContactPackage,
                ShareCredentialsFragment.KEY_CREDENTIAL_LIST to viewModel.selectedCredentials
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        RxHelper.unsubscribe(disposables)
    }
}