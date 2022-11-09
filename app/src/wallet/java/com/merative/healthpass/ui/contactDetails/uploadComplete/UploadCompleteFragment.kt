package com.merative.healthpass.ui.contactDetails.uploadComplete

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragUploadCompleteBinding
import com.merative.healthpass.extensions.getDrawableCompat
import com.merative.healthpass.extensions.orEmpty
import com.merative.healthpass.models.api.registration.uploadCredential.SubmitDataPayload
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.contactDetails.consent.CredentialsAdapter

class UploadCompleteFragment : BaseFragment() {

    private var _binding: FragUploadCompleteBinding? = null
    private val binding get() = _binding!!

    override val homeBtnEnabled: Boolean
        get() = false

    lateinit var viewModel: UploadCompleteVM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        handleBack()
        setHasOptionsMenu(true)

        _binding = FragUploadCompleteBinding.inflate(inflater, container, false)
        return binding.root
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(UploadCompleteVM::class.java)

        val (sharedList, failedList) = viewModel.filterList(
            (arguments?.get(KEY_CREDENTIALS_LIST) as? ArrayList<Package>).orEmpty(),
            arguments?.get(KEY_SUBMIT_RESPONSE) as? SubmitDataPayload
        )

        if (sharedList.isNotEmpty() && failedList.isEmpty()) {
            initUploadStatusView(
                getString(R.string.contact_uploadComplete_title),
                getString(R.string.contact_uploadComplete_subtitle),
                requireActivity().getDrawableCompat(R.drawable.ic_check_circle_black_48dp)
            )
        } else if (sharedList.isEmpty() && failedList.isNotEmpty()) {
            initUploadStatusView(
                getString(R.string.contact_uploadFailed_title),
                getString(R.string.contact_uploadFailed_subtitle),
                requireActivity().getDrawableCompat(R.drawable.ic_cancel_black_48dp)
            )
        } else if (sharedList.isNotEmpty() && failedList.isNotEmpty()) {
            initUploadStatusView(
                getString(R.string.contact_partiallySubmitted_title),
                getString(R.string.contact_partiallySubmitted_subtitle),
                requireActivity().getDrawableCompat(R.drawable.ic_exclamation_black_48dp)
            )
        }

        binding.uploadCompTxtShared.isVisible = sharedList.isNotEmpty()
        binding.recyclerViewSharedCredentials.isVisible = sharedList.isNotEmpty()

        binding.recyclerViewSharedCredentials.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = CredentialsAdapter(isSelectingEnabled = false).apply {
                setItems(sharedList)
            }
        }

        binding.uploadCompTxtFailed.isVisible = failedList.isNotEmpty()
        binding.recyclerViewFailedCredentials.isVisible = failedList.isNotEmpty()

        binding.recyclerViewFailedCredentials.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = CredentialsAdapter(isSelectingEnabled = false).apply {
                setItems(failedList)
            }
        }
    }

    private fun initUploadStatusView(
        statusTitle: String,
        statusSubtitle: String,
        statusIcon: Drawable?
    ) {
        binding.uploadStatusTitleTv.text = statusTitle
        binding.uploadStatusDescriptionTv.text = statusSubtitle
        binding.uploadStatusIcon.setImageDrawable(statusIcon)
    }

    //region menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_upload_complete, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_close -> {
                goToWallet()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //endregion

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_CREDENTIALS_LIST = "shared_list"
        const val KEY_SUBMIT_RESPONSE = "submit_response"
    }
}