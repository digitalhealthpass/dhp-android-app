package com.merative.healthpass.ui.contactDetails.associatedData

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragAssociatedDataBinding
import com.merative.healthpass.extensions.navigateSafe
import com.merative.healthpass.extensions.rxError
import com.merative.healthpass.models.Record
import com.merative.healthpass.models.api.registration.uploadCredential.AssociatedData
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.contactDetails.associatedData.AssociatedDataDetailsFragment.Companion.KEY_PACKAGE
import com.merative.healthpass.ui.contactDetails.associatedData.AssociatedDataDetailsFragment.Companion.KEY_RECORD
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AssociatedDataFragment : BaseFragment() {

    private var _binding: FragAssociatedDataBinding? = null
    private val binding get() = _binding!!

    private var contactPackage: ContactPackage? = null
    private val disposable = CompositeDisposable()

    private val recordSectionAdapter = SectionedRecyclerViewAdapter()
    private val clickDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactPackage = arguments?.get(KEY_PACKAGE_KEY) as ContactPackage
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragAssociatedDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = createViewModel(AssociatedDataVM::class.java)

        binding.buttonViewSource.setOnClickListener {
            navigateSafe(
                R.id.action_contactAssociatedDataFragment_to_contactAssociatedDataDetailsFragment,
                bundleOf(KEY_PACKAGE to contactPackage)
            )
        }

        showLoading(true)
        contactPackage?.let {
            disposable.add(
                viewModel.downloadAssociatedData(contactPackage!!)
                    .subscribe(this::initUI, errorConsumer())
            )
        }
    }

    private fun initUI(associatedData: AssociatedData) {
        showLoading(false)
        binding.recyclerViewRecords.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = recordSectionAdapter

            associatedData.cosPayload?.let { cosPayload ->
                if (cosPayload is JsonArray) {
                    val list = mutableListOf<Record>()
                    cosPayload.forEach {
                        if (it is JsonArray) {
                            for (i in 0 until it.size()) {
                                list.add(Record(it[i].toString()))
                            }
                        } else list.add(Record(it.toString()))
                    }
                    val section = RecordSection(getString(R.string.associated_data_owner_records))
                    section.setDataList(list)
                    recordSectionAdapter.addSection(
                        getString(R.string.associated_data_owner_records),
                        section
                    )
                    clickDisposable.add(
                        section.listenToClickEvents().subscribe(
                            {
                                findNavController().navigate(
                                    R.id.action_contactAssociatedDataFragment_to_contactAssociatedDataDetailsFragment,
                                    bundleOf(
                                        KEY_RECORD to it
                                    )
                                )
                            }, rxError("")
                        )
                    )
                }
            }
            associatedData.postBoxPayload?.let { postBoxPayload ->
                if (postBoxPayload is JsonObject) {
                    val attachments = postBoxPayload.getAsJsonArray("attachments")
                    val list = mutableListOf<Record>()
                    attachments.forEach {
                        list.add(Record(it.toString()))
                    }
                    val section = RecordSection(getString(R.string.associated_data_postbox_records))
                    section.setDataList(list)
                    if (list.isNotEmpty()) {
                        recordSectionAdapter.addSection(
                            getString(R.string.associated_data_postbox_records),
                            section
                        )
                    } else associatedData.postBoxPayload = null
                    clickDisposable.add(
                        section.listenToClickEvents().subscribe(
                            {
                                findNavController().navigate(
                                    R.id.action_contactAssociatedDataFragment_to_contactAssociatedDataDetailsFragment,
                                    bundleOf(KEY_RECORD to it)
                                )
                            }, rxError("")
                        )
                    )
                }
            }
            val noData = associatedData.cosPayload == null && associatedData.postBoxPayload == null
            binding.buttonViewSource.isVisible = !noData
            binding.recyclerViewRecords.isVisible = !noData
            binding.textViewNoData.isVisible = noData
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_PACKAGE_KEY = "keyPackage"
    }
}