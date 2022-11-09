package com.merative.healthpass.ui.region

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.merative.healthpass.databinding.FragRegionSelectionBinding
import com.merative.healthpass.extensions.handleCommonErrors
import com.merative.healthpass.extensions.rxError
import com.merative.healthpass.ui.common.baseViews.BaseBottomSheet
import com.merative.healthpass.utils.RxHelper
import io.reactivex.rxjava3.disposables.SerialDisposable

class RegionSelectionFragment : BaseBottomSheet() {

    private var _binding: FragRegionSelectionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val clickDisposable = SerialDisposable()
    private val saveDisposable = SerialDisposable()

    private lateinit var viewModel: RegionSelectionVM
    private lateinit var regionAdapter: RegionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        _binding = FragRegionSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(RegionSelectionVM::class.java)
        regionAdapter = RegionAdapter(viewModel)

        regionAdapter.addItems(viewModel.getRegionList())
        binding.regionRV.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = regionAdapter
        }

        binding.regionBtnDone.apply {
            setOnClickListener {
                saveRegion()
            }
        }

        binding.regionBtnDone.isEnabled = viewModel.currentEnv != null
    }

    private fun saveRegion() {
        showLoading(true)
        saveDisposable.set(
            viewModel.saveRegion()
                .doFinally { showLoading(false) }
                .subscribe({
                    flavorVM.credentialsDeleted()
                    setResult(true)
                }, {
                    handleCommonErrors(it)
                })
        )
    }

    override fun onResume() {
        super.onResume()
        clickDisposable.set(
            regionAdapter.listenToClickEvents()
                .subscribe({ pair ->
                    binding.regionBtnDone.isEnabled = viewModel.currentEnv != null
                }, rxError("Failed to Listen to Clicks"))
        )
    }

    override fun onDestroyView() {
        RxHelper.unsubscribe(clickDisposable.get(), saveDisposable.get())
        super.onDestroyView()
        _binding = null
    }
}