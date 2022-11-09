package com.merative.healthpass.ui.registration

import android.app.Activity
import android.content.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.core.graphics.drawable.toDrawable
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.merative.healthpass.R
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import io.reactivex.rxjava3.subjects.PublishSubject

open class BaseRegistrationFragment : BaseFragment() {
    protected val smsEvents: PublishSubject<String> = PublishSubject.create()
    private val smsVerificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        // Get consent intent
                        val consentIntent =
                            extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                        try {
                            // Start activity to show consent dialog to user, activity must be started in
                            // 5 minutes, otherwise you'll receive another TIMEOUT intent
                            startActivityForResult(consentIntent, SMS_CONSENT_REQUEST)
                        } catch (e: ActivityNotFoundException) {
                            loge("failed to parse sms", e, true)
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> {
                        // Time out occurred, handle the error.
                    }
                }
            }
        }
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        applyAppBarLayout {
            setExpanded(false)
        }
        handleBack()
    }

    private fun handleBack() {
        applySupportActionBar {
            setHomeAsUpIndicator(R.drawable.ic_cancel_blue_24dp)
            setHomeActionContentDescription(R.string.button_title_cancel)
            setBackgroundDrawable(activity?.getColor(R.color.black)?.toDrawable())
        }

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    view?.hideKeyboard()
                    showBackWarning()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    protected fun showBackWarning() {
        activity?.showDialog(
            getString(R.string.warning),
            getString(R.string.dialog_back_registration_message),
            getString(R.string.button_title_ok), {
                showLoading(false)
                goToWallet()
            },
            getString(R.string.button_title_cancel), {}
        )
    }

    /**
     * Keep in mind that the receiver works for one time only
     */
    protected fun registerSmsReceiver() {
        SmsRetriever.getClient(requireActivity()).startSmsUserConsent(null)
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        activity?.registerReceiver(smsVerificationReceiver, intentFilter)
    }

    protected fun unregisterSmsReceiver() {
        smsVerificationReceiver.unregisterReceiver(activity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            // ...
            SMS_CONSENT_REQUEST ->
                // Obtain the phone number from the result
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Get SMS message content
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    // Extract one-time code from the message and complete verification
                    // `message` contains the entire text of the SMS message, so you will need
                    // to parse the string.
                    val oneTimeCode = parseOneTimeCode(message.orEmpty())
                    logd("oneTimeCode: $oneTimeCode")
                    smsEvents.onNext(oneTimeCode)
                    // send one time code to the server
                } else {
                    // Consent denied. User can type OTC manually.
                }
        }
    }

    private fun parseOneTimeCode(message: String): String {
        return message.substring(message.indexOf(":") + 1).trim()
    }

    protected fun isNIHFlow(orgName: String): Boolean {
        return orgName == ContactPackage.TYPE_NIH
    }

    companion object {
        private val SMS_CONSENT_REQUEST = 2
    }
}