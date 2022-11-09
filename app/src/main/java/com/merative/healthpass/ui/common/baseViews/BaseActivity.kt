package com.merative.healthpass.ui.common.baseViews

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.merative.healthpass.BuildConfig
import com.merative.healthpass.common.App
import com.merative.healthpass.common.sensor.ShakeListener
import com.merative.healthpass.extensions.orValue
import com.merative.healthpass.extensions.showDebugDialog
import com.merative.healthpass.utils.RxHelper
import io.reactivex.rxjava3.disposables.SerialDisposable
import javax.inject.Inject

open class BaseActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val shakeListener: ShakeListener by lazy { ShakeListener(this) }
    private val shakeDisposable = SerialDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as App).appComponent.inject(this)
    }

    protected fun <VM : ViewModel> createViewModel(modelClass: Class<VM>): VM {
        return ViewModelProvider(this, viewModelFactory).get(modelClass)
    }

    override fun onResume() {
        super.onResume()
        initShakeListener()

        if (!BuildConfig.DEBUG) {
            // Re-enable screenshot capture ability when the activity is resumed, so that if this is due to the application
            //  being put back into the foreground, this will allow screenshot to be captured on the app
            window?.setFlags(0, WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun onPause() {
        super.onPause()
        shakeListener.onPause()
        RxHelper.unsubscribe(shakeDisposable.get())

        if (!BuildConfig.DEBUG) {
            // Disable screenshot capture ability when the activity is paused, so that if this is due to the application
            //  being put into background, this will make the app's image to be blank out in the multi-app-selection list.
            //  This is the security requirement
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
    }

    private fun initShakeListener() {
        if (BuildConfig.DEBUG) {
            shakeListener.onResume()
            shakeDisposable.set(
                shakeListener.listenToShakeEvents()
                    .subscribe({
                        showDebugDialog()
                    }, {
                        Toast.makeText(
                            this,
                            it.localizedMessage.orValue("failed to listen to initiate the share event"),
                            Toast.LENGTH_SHORT
                        ).show()
                    })
            )
        }
    }
}
