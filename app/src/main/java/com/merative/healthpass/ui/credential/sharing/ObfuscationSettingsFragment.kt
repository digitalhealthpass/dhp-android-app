package com.merative.healthpass.ui.credential.sharing

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonElement
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragObfuscationSettingsBinding
import com.merative.healthpass.extensions.toArrayList
import com.merative.healthpass.extensions.toHashMapWithKey
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import com.merative.healthpass.ui.credential.barcode.FullScreenQRFragment
import com.merative.watson.healthpass.verifiablecredential.extensions.asJsonArrayOrNull
import com.merative.watson.healthpass.verifiablecredential.extensions.stringfy
import com.merative.watson.healthpass.verifiablecredential.extensions.toJsonElement
import com.merative.watson.healthpass.verifiablecredential.models.veriableObject.VerifiableObject

class ObfuscationSettingsFragment : BaseFragment() {
    private var _binding: FragObfuscationSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var aPackage: Package
    private var menuFlow: Int = 0
    lateinit var viewModel: ObfuscationSettingsVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            aPackage = it.get(KEY_CREDENTIAL_PACKAGE) as Package
            menuFlow = it.getInt(KEY_MENU_FUNCTION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragObfuscationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(ObfuscationSettingsVM::class.java)
        viewModel.obfuscationObjectList =
            aPackage.verifiableObject.credential!!.obfuscation.asJsonArrayOrNull()
                .toHashMapWithKey { true }
        setHasOptionsMenu(true)
        loadCardView()
        loadRV()
    }

    private fun loadRV() {
        val obfuscationArrayList =
            aPackage.verifiableObject.credential!!.obfuscation.asJsonArrayOrNull()
                ?.toList().toArrayList()
        val adapter = ObfuscatedFieldAdapter(viewModel)
        adapter.addItems(obfuscationArrayList)

        binding.shareSettingsRv.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
    }

    private fun loadCardView() {
        ViewUtils.adjustCredentialView(binding.shareSettingsCardview, aPackage)
    }

    private fun getFilteredPackage(): Package {
        val filteredListOfObfuscatedValues = ArrayList<JsonElement>()
        viewModel.obfuscationObjectList.forEach {
            if (it.value) {
                filteredListOfObfuscatedValues.add(it.key)
            }
        }
        val modifiedCredential =
            aPackage.verifiableObject.credential!!.copy(obfuscation = filteredListOfObfuscatedValues.toJsonElement().asJsonArray)
        return aPackage.copy(verifiableObject = VerifiableObject(modifiedCredential.stringfy()))
    }

    private fun showFullScreen() {
        val bundle =
            bundleOf(FullScreenQRFragment.KEY_CREDENTIAL_PACKAGE to getFilteredPackage())
        findNavController().navigate(R.id.fullScreenQRFragment, bundle)
    }

    //region menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        when (menuFlow) {
            TO_QR -> {
                inflater.inflate(R.menu.menu_obfuscation_show_qr_settings, menu)
            }
            TO_SHARE -> {
                inflater.inflate(R.menu.menu_obfuscation_share_settings, menu)
            }
            TO_ADD_TO_GPAY -> {
                inflater.inflate(R.menu.menu_obfuscation_add_to_gpay_settings, menu)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_share -> {
                showShareCredentialDialog(getFilteredPackage())
                true
            }
            R.id.menu_show_qr -> {
                showFullScreen()
                true
            }
            R.id.menu_gpay -> {
                addToGPay()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    //endregion

    private fun addToGPay() {
        //TODO ADD TO GPAY FUNCTIONALITY
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val KEY_CREDENTIAL_PACKAGE = "credPack"
        const val KEY_MENU_FUNCTION = "menuFunction"

        const val TO_QR = 0
        const val TO_SHARE = 1
        const val TO_ADD_TO_GPAY = 2
    }
}