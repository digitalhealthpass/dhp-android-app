package com.merative.healthpass.ui.contactDetails.shareCred

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragShareCredentialsBinding
import com.merative.healthpass.extensions.addIfNotExist
import com.merative.healthpass.extensions.isNotNullOrEmpty
import com.merative.healthpass.extensions.orEmpty
import com.merative.healthpass.extensions.setImageFromBase64
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.contactDetails.consent.ConsentFragment
import com.merative.watson.healthpass.verifiablecredential.extensions.getStringOrNull
import com.merative.watson.healthpass.verifiablecredential.models.credential.getFirstPiiController
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

class ShareCredentialsFragment : BaseFragment() {

    private var _binding: FragShareCredentialsBinding? = null
    private val binding get() = _binding!!

    private var cardsSectionAdapter = SectionedRecyclerViewAdapter()
    private val credentialSection = ShareCredentialsSection()

    val contactPackage: ContactPackage by lazy { arguments?.get(ConsentFragment.KEY_CONTACT) as ContactPackage }
    var selectedCredentials = ArrayList<Package>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)

        _binding = FragShareCredentialsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contact = arguments?.get(ConsentFragment.KEY_CONTACT) as? ContactPackage

        selectedCredentials = (arguments?.get(KEY_CREDENTIAL_LIST) as? ArrayList<Package>).orEmpty()

        binding.cardsRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = cardsSectionAdapter
        }

        setCredentialsList()

        if (contact != null) {
            setContact(contact)
        }

        binding.shareCardBtn.setOnClickListener { navigateToConsent() }
    }

    private fun setCredentialsList() {
        cardsSectionAdapter.addIfNotExist(credentialSection, 0)
        credentialSection.setDataList(selectedCredentials)
        credentialSection.setHasHeader(false)
        cardsSectionAdapter.getAdapterForSection(credentialSection)
            .notifyAllItemsChanged()

        if (selectedCredentials.size > 1) {
            binding.cardsHeaderTextView.text = getString(R.string.cards_selected)
        }
    }

    private fun setContact(contact: ContactPackage) {
        binding.contactsContainer.imgLeft.setImageFromBase64(contact.profilePackage?.issuerMetaData?.metadata?.logo, null)

        val subTitle: String = if (contact.isDefaultPackage()) {
            ""//model.profilePackage?.credential?.credentialSubject?.getStringOrNull("contact").orEmpty()
        } else {
            contact.profilePackage?.verifiableObject?.credential
                ?.getFirstPiiController()?.getStringOrNull("contact").orEmpty()
        }
        binding.contactsContainer.title.text = contact.getContactName()

        binding.contactsContainer.subTitle.apply {
            isVisible = subTitle.isNotNullOrEmpty()
            text = subTitle
        }
    }

    private fun navigateToConsent() {
        findNavController().navigate(
            R.id.action_shareFragment_to_consentFragment,
            bundleOf(
                ConsentFragment.KEY_CONTACT to arguments?.get(ConsentFragment.KEY_CONTACT) as? ContactPackage,
                ConsentFragment.KEY_CREDENTIAL_LIST to selectedCredentials
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_CONTACT = "contact"
        const val KEY_CREDENTIAL_LIST = "credential_package_list"
    }
}