package com.merative.healthpass.ui.organizations_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragOrganizationsListBinding
import com.merative.healthpass.extensions.addIfNotExist
import com.merative.healthpass.extensions.rxError
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.organization.OrganizationDetailsFragment
import com.merative.healthpass.utils.RxHelper
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.SerialDisposable

class OrganizationsListFragment : BaseFragment() {
    private var _binding: FragOrganizationsListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    protected val binding get() = _binding!!

    private lateinit var viewModel: OrganizationsListViewModel

    private val clickDisposable = CompositeDisposable()
    private val loadingDisposable = SerialDisposable()

    private val sectionAdapter = SectionedRecyclerViewAdapter()

    private lateinit var availableOrgsListSection: OrganizationsListSection
    private lateinit var expiredOrgsListSection: OrganizationsListSection

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        _binding = FragOrganizationsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(OrganizationsListViewModel::class.java)

        availableOrgsListSection =
            OrganizationsListSection(getString(R.string.OrganizationList_header_title1))
        expiredOrgsListSection =
            OrganizationsListSection(getString(R.string.OrganizationList_header_title2))

        binding.recyclerViewAvailableOrganizations.adapter = sectionAdapter

        binding.recyclerViewAvailableOrganizations.apply {
            layoutManager = LinearLayoutManager(activity)
            availableOrgsListSection.tag = TAG_AVAILABLE
            expiredOrgsListSection.tag = TAG_EXPIRED

            adapter = sectionAdapter
        }

        loadData()

        clickDisposable.addAll(
            availableOrgsListSection.customListenToClickEvents(),
            expiredOrgsListSection.customListenToClickEvents()
        )
    }

    override fun onResume() {
        super.onResume()
        setTitle(getString(R.string.Profile_currentOrganization_title))
    }

    private fun loadData() {
        showLoading(true)
        loadingDisposable.set(
            viewModel.loadData()
                .doFinally { showLoading(false) }
                .subscribe(
                    {
                        if (it.first.isEmpty() && it.second.isEmpty()) {
                            binding.recyclerViewAvailableOrganizations.visibility = View.GONE
                            binding.noOrgMessageTv.visibility = View.VISIBLE
                        } else {
                            binding.recyclerViewAvailableOrganizations.visibility = View.VISIBLE
                            binding.noOrgMessageTv.visibility = View.GONE
                            if (it.first.isNotEmpty()) {
                                sectionAdapter.addIfNotExist(availableOrgsListSection, 0)

                                availableOrgsListSection.setDataList(it.first)
                                sectionAdapter.getAdapterForSection(availableOrgsListSection)
                                    .notifyAllItemsChanged()
                            }

                            if (it.second.isNotEmpty()) {
                                sectionAdapter.addIfNotExist(expiredOrgsListSection)

                                expiredOrgsListSection.setDataList(it.second)
                                sectionAdapter.getAdapterForSection(expiredOrgsListSection)
                                    .notifyAllItemsChanged()
                            }
                        }
                    }, rxError("failed to load data")
                )
        )
    }

    private fun OrganizationsListSection.customListenToClickEvents(): Disposable {
        return this.listenToClickEvents()
            .subscribe(
                { value ->
                    findNavController().navigate(
                        R.id.action_Scann_to_org_details, bundleOf(
                            OrganizationDetailsFragment.KEY_CREDENTIAL_PACKAGE to value
                        )
                    )
                }, rxError("failed to listen to clicks")
            )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        RxHelper.unsubscribe(
            clickDisposable,
            loadingDisposable.get()
        )
        sectionAdapter.removeAllSections()
    }

    companion object {
        const val TAG_AVAILABLE = "available"
        const val TAG_EXPIRED = "expired"
    }
}