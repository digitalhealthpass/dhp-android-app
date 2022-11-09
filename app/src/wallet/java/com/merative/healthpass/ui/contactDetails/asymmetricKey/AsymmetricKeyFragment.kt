package com.merative.healthpass.ui.contactDetails.asymmetricKey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragAsymmetricKeyBinding
import com.merative.healthpass.models.credential.AsymmetricKey
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.watson.healthpass.verifiablecredential.extensions.format
import java.util.*
import kotlin.collections.ArrayList

class AsymmetricKeyFragment : BaseFragment() {

    private var _binding: FragAsymmetricKeyBinding? = null
    private val binding get() = _binding!!

    private var contactPackage: ContactPackage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactPackage = arguments?.get(KEY_PACKAGE_KEY) as ContactPackage
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragAsymmetricKeyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = createViewModel(AsymmetricKeyVM::class.java)

        contactPackage?.asymmetricKey?.let {
            initUI(it)
        }
    }

    private fun initUI(asKey: AsymmetricKey) {
        binding.associatedKeyDetailsRv.layoutManager = LinearLayoutManager(requireContext())
        val adapter = KeyDataAdapter()
        val keyDataList = ArrayList<Pair<String, CharSequence>>()
        keyDataList.add(
            Pair(getString(R.string.tag), asKey.tag)
        )
        keyDataList.add(
            Pair(getString(R.string.created), Date(asKey.timeStamp).format())
        )
        keyDataList.add(
            Pair(getString(R.string.public_key_label), asKey.publicKey)
        )
        binding.associatedKeyDetailsRv.adapter = adapter
        adapter.setItems(keyDataList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_PACKAGE_KEY = "keyPackage"
    }
}