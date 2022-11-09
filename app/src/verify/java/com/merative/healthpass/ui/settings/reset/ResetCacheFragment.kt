package com.merative.healthpass.ui.settings.reset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragResetCacheBinding
import com.merative.healthpass.extensions.rxError
import com.merative.healthpass.extensions.showDialog
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.utils.RxHelper
import io.reactivex.rxjava3.disposables.SerialDisposable

class ResetCacheFragment : BaseFragment() {

    private var _binding: FragResetCacheBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val resetCacheDisposable = SerialDisposable()
    private val sizeDisposable = SerialDisposable()
    private lateinit var viewModel: ResetCacheViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragResetCacheBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = createViewModel(ResetCacheViewModel::class.java)

        calculateSize()
        binding.buttonResetIssuer.setOnClickListener {
            activity?.showDialog(
                getString(R.string.profile_schema_resetCache),
                getString(R.string.profile_schema_resetCache_reset),
                getString(R.string.profile_resetSchema_action), { _ -> resetCache() },
                getString(R.string.button_title_cancel), {}
            )
        }
    }

    private fun resetCache() {
        resetCacheDisposable.set(
            viewModel.resetCache()
                .subscribe({
                    calculateSize()
                    flavorVM.selectedCredential = null
                }, rxError("failed to reset cache"))
        )
    }

    private fun calculateSize() {
        sizeDisposable.set(
            viewModel.getSize().subscribe({
                binding.cacheStorageSubtext.text =
                    getString(
                        R.string.profile_reset_footer1Formant,
                        "$it ${getString(R.string.profile_reset_footer_bytes)}"
                    )
            }, rxError("failed to calculate size"))
        )

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        RxHelper.unsubscribe(resetCacheDisposable, sizeDisposable)
    }
}