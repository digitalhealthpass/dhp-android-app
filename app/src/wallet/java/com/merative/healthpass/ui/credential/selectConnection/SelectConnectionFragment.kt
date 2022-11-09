package com.merative.healthpass.ui.credential.selectConnection

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragSelectConnectionBinding
import com.merative.healthpass.extensions.orEmpty
import com.merative.healthpass.extensions.rxError
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.contactDetails.consent.ConsentFragment
import com.merative.healthpass.ui.contactDetails.shareCred.ShareCredentialsFragment
import io.reactivex.rxjava3.disposables.CompositeDisposable

class SelectConnectionFragment : BaseFragment() {

    private var _binding: FragSelectConnectionBinding? = null
    private val binding get() = _binding!!

    private var menuNext: MenuItem? = null

    lateinit var viewModel: SelectConnectionsVM

    private val availableConnectionsAdapter = ConnectionsAdapter()
    private val disposables = CompositeDisposable()

    private var selectedCredentials = ArrayList<Package>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)

        _binding = FragSelectConnectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(SelectConnectionsVM::class.java)

        selectedCredentials = (arguments?.get(ShareCredentialsFragment.KEY_CREDENTIAL_LIST) as? ArrayList<Package>).orEmpty()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = availableConnectionsAdapter
        }

        disposables.add(
            viewModel.loadAllConnections()
                .subscribe({
                    availableConnectionsAdapter.setItems(it)
                    binding.recyclerView.isVisible = it.isNotEmpty()
                    binding.availableConnectionHeader.isVisible = it.isNotEmpty()
                }, rxError("couldn't load the connections"))
        )

        disposables.add(
            availableConnectionsAdapter.listenToClickEvents()
                .subscribe({
                    viewModel.removeAndAddContacts(it.second)
                    activity?.invalidateOptionsMenu()
                }, rxError("failed to listen to clicks"))
        )
        binding.btnQrCamera.setOnClickListener { navigateToRegistration() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_credentials, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menuNext = menu.findItem(R.id.menu_next)
        menuNext?.isVisible = viewModel.selectedConnections.size != 0
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_next -> {
                navigateToConsent()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToConsent() {
        findNavController().navigate(
            R.id.action_selectConnectionFragment_to_consentFragment,
            bundleOf(
                ConsentFragment.KEY_CONTACT to viewModel.selectedConnections[0] as? ContactPackage,
                ConsentFragment.KEY_CREDENTIAL_LIST to selectedCredentials
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun navigateToRegistration() {
        findNavController().navigate(R.id.action_selectConnectionFragment_to_registrationFragment)
    }

    companion object {
        const val KEY_CREDENTIAL_LIST = "credential_package_list"
    }
}