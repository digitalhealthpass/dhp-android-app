package com.merative.healthpass.ui.credential.details

import android.os.Bundle
import androidx.annotation.CallSuper
import com.merative.healthpass.common.App
import com.merative.healthpass.extensions.showActionBar
import com.merative.healthpass.ui.common.baseViews.BaseFragment

abstract class CredentialDetailsFragment : BaseFragment() {

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().applicationContext as App).appComponent
            .inject(this)
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        showActionBar()
        enableBackButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        showLoading(false)
    }

    companion object {
        const val KEY_CREDENTIAL_PACKAGE = "credentials_package"
        const val REQUEST_CODE_SHARE = 1
    }
}