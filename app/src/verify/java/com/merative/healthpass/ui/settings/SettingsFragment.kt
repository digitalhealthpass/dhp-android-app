package com.merative.healthpass.ui.settings

import android.view.View
import androidx.core.view.isVisible
import com.merative.healthpass.R
import com.merative.healthpass.extensions.isEnabledAlpha
import com.merative.healthpass.extensions.navigateSafe
import com.merative.healthpass.utils.RxHelper
import io.reactivex.rxjava3.disposables.SerialDisposable

class SettingsFragment : BaseSettingsFragment() {

    private val deleteAllDisposable = SerialDisposable()

    override fun onDestroyView() {
        super.onDestroyView()
        RxHelper.unsubscribe(deleteAllDisposable.get())
    }

    override fun setupTopUI(root: View) {
        binding.buttonKiosk.isVisible = true
        binding.textViewKiosk.isVisible = true
        binding.buttonSound.isVisible = true
        binding.textViewSound.isVisible = true

        binding.settingsTopLayout.lviOrganizations.setOnClickListener {
            navigateSafe(R.id.global_action_to_contacts_list_frag)
        }
        binding.buttonKiosk.setOnClickListener {
            navigateSafe(R.id.action_settings_to_kiosk)
        }
        binding.buttonSound.setOnClickListener {
            navigateSafe(R.id.action_settings_to_sound)
        }
        binding.settingsTopLayout.switchRevoke.isEnabledAlpha = false
        binding.settingsTopLayout.switchRevoke.setOnCheckedChangeListener { buttonView, isChecked ->
            //TODO
        }
    }
}