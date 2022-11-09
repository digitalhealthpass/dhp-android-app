package com.merative.healthpass.ui.home

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragHomeBinding
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.region.getEnvironmentTitle
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.contactDetails.details.ContactDetailsFragment
import com.merative.healthpass.ui.credential.details.CredentialDetailsFragment
import com.merative.healthpass.utils.RxHelper
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class HomeFragment : BaseFragment() {
    override val homeBtnEnabled: Boolean
        get() = false

    private var _binding: FragHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel

    private val clickDisposable = CompositeDisposable()
    private val loadingDisposable = CompositeDisposable()

    private var cardsSectionAdapter = SectionedRecyclerViewAdapter()
    private var connectionsSectionAdapter = SectionedRecyclerViewAdapter()
    private val credentialSection = CredentialSection()
    private val contactsSection = ContactsSection()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragHomeBinding.inflate(inflater, container, false)
        val root = binding.root

        viewModel = createViewModel(HomeViewModel::class.java)

        setHasOptionsMenu(true)
        handleBack()

        binding.cardsAddImg.setOnClickListener {
            navigateToScan()
        }

        binding.cardsCardView.setOnClickListener {
            navigateToScan()
        }

        binding.connectionsAddImg.setOnClickListener {
            navigateToRegistration()
        }

        binding.connectionsCardView.setOnClickListener {
            navigateToRegistration()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardsRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = cardsSectionAdapter
        }

        binding.connectionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = connectionsSectionAdapter
        }

        viewModel.issuerListJson =
            context?.loadAssetsFileAsString("trust-list-SHC.json").orValue("{}")
        loadData()

        clickDisposable.add(
            credentialSection.listenToClickEvents()
                .subscribe({

                    val nav = findNavController()
                    if (nav.currentDestination?.id == R.id.navigation_home_fragment) {
                        val bundle = bundleOf(
                            CredentialDetailsFragment.KEY_CREDENTIAL_PACKAGE to it
                        )
                        nav.navigate(R.id.global_action_credential_details, bundle)
                    }
                }, rxError("failed to listen to clicks"))
        )

        clickDisposable.add(
            contactsSection.listenToClickEvents()
                .subscribe({
                    val nav = findNavController()
                    if (nav.currentDestination?.id == R.id.navigation_home_fragment) {
                        val bundle = bundleOf(
                            ContactDetailsFragment.KEY_CONTACT_PACKAGE to it
                        )
                        nav.navigate(
                            R.id.action_navigation_wallet_to_contactDetailsFragment,
                            bundle
                        )
                    }
                }, rxError("failed to listen to clicks"))
        )
    }

    private fun loadData() {
        showLoading(true)
        loadingDisposable.add(
            viewModel.loadData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally { showLoading(false) }
                .subscribe({
                    if (it.first.isNotEmpty()) {
                        cardsSectionAdapter.addIfNotExist(credentialSection, 0)

                        credentialSection.setDataList(it.first)
                        credentialSection.setHasHeader(false)
                        cardsSectionAdapter.getAdapterForSection(credentialSection)
                            .notifyAllItemsChanged()
                    } else {
                        cardsSectionAdapter.removeSection(credentialSection)
                    }

                    if (it.second.isNotEmpty()) {
                        connectionsSectionAdapter.addIfNotExist(contactsSection, 0)

                        contactsSection.setDataList(it.second)
                        contactsSection.setHasHeader(false)
                        connectionsSectionAdapter.getAdapterForSection(contactsSection)
                            .notifyAllItemsChanged()
                    } else {
                        connectionsSectionAdapter.removeSection(contactsSection)
                    }

                    updateVisibility()
                }, rxError("failed to load data"))
        )
    }

    //region menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    //endregion

    private fun navigateToScan() {
        navigateSafe(R.id.action_wallet_to_scanChooserFragment)
    }

    private fun navigateToRegistration() {
        findNavController().navigate(R.id.action_navigation_wallet_to_registration)
    }

    private fun updateVisibility() {
        val isCardsAdapterEmpty = cardsSectionAdapter.itemCount == 0
        binding.cardsCardView.isVisible = isCardsAdapterEmpty
        binding.cardsRecyclerView.isVisible = !isCardsAdapterEmpty

        val isConnectionsAdapterEmpty = connectionsSectionAdapter.itemCount == 0
        if (viewModel.getEnvironment().getEnvironmentTitle(resources).contains("USA")) {
            binding.connectionsCardView.isVisible = isConnectionsAdapterEmpty
            binding.connectionsRecyclerView.isVisible = !isConnectionsAdapterEmpty
            binding.connectionsTextView.isVisible = true
            binding.connectionsTextView.isVisible = true
            binding.connectionsAddImg.isVisible = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        RxHelper.unsubscribe(
            clickDisposable,
            loadingDisposable
        )
        _binding = null
    }

    private fun handleBack() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    mainActivityVM.reset()
                    activity?.finishAffinity()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    companion object {
        const val KEY_CONTACT_CRED_ID = "cred"
    }
}