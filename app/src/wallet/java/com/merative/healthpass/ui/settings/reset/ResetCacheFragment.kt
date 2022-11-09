package com.merative.healthpass.ui.settings.reset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.merative.healthpass.R
import com.merative.healthpass.databinding.FragResetCacheBinding
import com.merative.healthpass.extensions.rxError
import com.merative.healthpass.extensions.showDialog
import com.merative.healthpass.models.sharedPref.AutoReset
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.common.recyclerView.ViewUtils
import com.merative.healthpass.utils.RxHelper
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import java.util.*

class ResetCacheFragment : BaseFragment() {

    protected var _binding: FragResetCacheBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    protected val binding get() = _binding!!

    protected val deleteAllDisposable = SerialDisposable()
    protected val loadSettingsDisposable = CompositeDisposable()
    protected lateinit var viewModel: ResetCacheViewModel

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
    }

    override fun onResume() {
        super.onResume()
        initButtons()
        setupAutomaticReset()
        calculateSize()
        loadCredentialsCount()
    }

    private fun loadCredentialsCount() {
        loadSettingsDisposable.add(
            viewModel.loadAllCredentialsCount()
                .subscribe({
                    binding.deleteAllCardsSubtext.text =
                        getString(R.string.profile_reset_footer0Formant, it.first.toString())
                }, rxError("Could not load all Credentials"))
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        RxHelper.unsubscribe(deleteAllDisposable.get(), loadSettingsDisposable)
        _binding = null
    }

    protected fun calculateSize() {
        binding.cacheStorageSubtext.text =
            getString(
                R.string.profile_reset_footer1Formant,
                "${viewModel.getSize()} ${getString(R.string.profile_reset_footer_bytes)}"
            )
    }

    private fun initButtons() {
        binding.buttonDeleteAllCards.setOnClickListener {
            activity?.showDialog(
                getString(R.string.erase_credentials_title),
                getString(R.string.erase_credentials_message),
                getString(R.string.kpm_delete_confirm),
                {
                    deleteAllCredentials()
                },
                getString(R.string.button_title_cancel),
                {}
            )
        }

        binding.buttonResetCache.setOnClickListener {
            showDeleteAlertDialog(
                R.string.profile_schema_resetCache,
                R.string.profile_resetCache_message,
                R.string.profile_schema_resetCache,
                R.string.button_title_cancel,
                TYPE_ALL
            )
        }
    }

    private fun showDeleteAlertDialog(
        @StringRes title: Int,
        @StringRes body: Int,
        @StringRes positiveText: Int,
        @StringRes negativeText: Int,
        type: Int
    ) {
        activity?.showDialog(
            getString(title),
            getString(body),
            getString(positiveText), { _ ->
                when (type) {
                    TYPE_SCHEMA -> deleteAllSchema()
                    TYPE_METADATA -> deleteAllMetadata()
                    TYPE_ALL -> resetAllCache()
                }
            },
            getString(negativeText), {}
        )
    }

    private fun deleteAllSchema() {
        deleteAllDisposable.set(
            viewModel.deleteAllSchema()
                .subscribe({
                    calculateSize()
                }, rxError("failed to delete all schemas"))
        )
    }

    private fun deleteAllMetadata() {
        deleteAllDisposable.set(
            viewModel.deleteAllMetadata()
                .subscribe({
                    calculateSize()
                }, rxError("failed to delete all metadata"))
        )
    }

    private fun resetAllCache() {
        deleteAllDisposable.set(
            viewModel.resetCache()
                .subscribe({
                    calculateSize()
                }, rxError("failed to delete all metadata"))
        )
    }

    private fun deleteAllCredentials() {
        deleteAllDisposable.set(
            viewModel.deleteAllCredentialsAndSchema()
                .subscribe({
                    calculateSize()
                    flavorVM.credentialsDeleted()
                    loadCredentialsCount()
                }, rxError("failed to delete all credential and schema"))
        )
    }

    private fun setupAutomaticReset() {
        ViewUtils.bindSimpleView(
            binding.resetFrequencyContainer.root,
            getString(R.string.profile_resetFrequency),
            getFrequencyTextValue(viewModel.getAutoResetFrequency()),
            false,
            R.drawable.ic_chevron_right
        )
        binding.dividerAutoReset.isVisible = viewModel.getAutoResetFrequencyEnabled()
        binding.resetFrequencyContainer.root.isVisible =
            viewModel.getAutoResetFrequencyEnabled()
        binding.switchAutoReset.apply {
            isChecked = viewModel.getAutoResetFrequencyEnabled()
            setOnCheckedChangeListener { _, isChecked ->
                loadSettingsDisposable.add(
                    viewModel.setAutoResetFrequencyEnabled(
                        isChecked,
                        viewModel.getAutoResetFrequency(),
                        Calendar.getInstance()
                    )
                        .subscribe({
                            binding.dividerAutoReset.isVisible = isChecked
                            binding.resetFrequencyContainer.root.isVisible = isChecked
                        }, rxError("failed to set auto reset feature"))
                )
            }
        }

        binding.resetFrequencyContainer.root.setOnClickListener {
            showFrequencyListDialog()
        }
        binding.autoResetDescription.text =
            getString(
                R.string.profile_reset_footer2Formant,
                viewModel.getLastResetDate(requireContext())
            )
    }

    private fun showFrequencyListDialog() {
        // setup the alert builder
        val listDialog = MaterialAlertDialogBuilder(requireContext())
        listDialog.setTitle(getString(R.string.profile_resetFrequency))

        val frequencyList = arrayOf(
            getString(R.string.profile_daily),
            getString(R.string.profile_weekly),
            getString(R.string.profile_monthly),
            getString(R.string.button_title_cancel)
        )

        listDialog.setItems(
            frequencyList
        ) { _, which ->
            when (frequencyList[which]) {
                getString(R.string.profile_daily) -> {
                    setResetFrequencyFromDialog(AutoReset.TYPE_DAILY)
                }
                getString(R.string.profile_weekly) -> {
                    setResetFrequencyFromDialog(AutoReset.TYPE_WEEKLY)
                }
                getString(R.string.profile_monthly) -> {
                    setResetFrequencyFromDialog(AutoReset.TYPE_MONTHLY)
                }
            }
        }
        val alert = listDialog.create()
        alert.setCanceledOnTouchOutside(true)
        alert.show()
    }

    private fun setResetFrequencyFromDialog(type: Int) {
        showLoading(true)
        loadSettingsDisposable.add(
            viewModel.setAutoResetFrequencyEnabled(
                viewModel.getAutoResetFrequencyEnabled(),
                type
            )
                .doFinally { showLoading(false) }
                .subscribe({
                    binding.resetFrequencyContainer.txtRight.text =
                        getFrequencyTextValue(viewModel.getAutoResetFrequency())
                }, rxError("Failed to Save Auto Reset Preference"))
        )
    }

    private fun getFrequencyTextValue(type: Int): String {
        return when (type) {
            AutoReset.TYPE_DAILY -> getString(R.string.profile_daily)
            AutoReset.TYPE_WEEKLY -> getString(R.string.profile_weekly)
            else -> getString(R.string.profile_monthly)
        }
    }

    companion object {
        //Delete Types
        const val TYPE_ALL = 0
        const val TYPE_SCHEMA = 1
        const val TYPE_METADATA = 2
        const val TYPE_ISSUER = 3
    }
}