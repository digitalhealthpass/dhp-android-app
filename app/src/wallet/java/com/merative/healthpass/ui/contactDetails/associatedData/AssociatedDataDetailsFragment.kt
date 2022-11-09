package com.merative.healthpass.ui.contactDetails.associatedData

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.GsonBuilder
import com.merative.healthpass.databinding.FragAssociatedDataDetailsBinding
import com.merative.healthpass.models.Record
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.watson.healthpass.verifiablecredential.extensions.toJsonElement
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AssociatedDataDetailsFragment : BaseFragment() {

    private var _binding: FragAssociatedDataDetailsBinding? = null
    private val binding get() = _binding!!

    private var contactPackage: ContactPackage? = null
    private var record: Record? = null
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactPackage = arguments?.get(KEY_PACKAGE) as? ContactPackage
        record = arguments?.get(KEY_RECORD) as? Record
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragAssociatedDataDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = createViewModel(AssociatedDataVM::class.java)
        contactPackage?.let {
            showLoading(true)
            disposable.add(
                viewModel.downloadAssociatedDataString(contactPackage!!)
                    .subscribe(this::initUI, errorConsumer())
            )
        }

        record?.let {
            val gson = GsonBuilder().setPrettyPrinting().create()
            val formattedJson = gson.toJson(it.data.toJsonElement())
            initUI(formattedJson)
        }
    }

    private fun initUI(associatedData: String) {
        showLoading(false)
        binding.associatedDataText.text = associatedData
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
        const val KEY_PACKAGE = "keyPackage"
        const val KEY_RECORD = "keyRecord"
    }
}