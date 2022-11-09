package com.merative.healthpass.ui.common.baseViews

import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.annotation.CheckResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.merative.healthpass.R
import com.merative.healthpass.common.App
import com.merative.healthpass.extensions.*
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.credential.details.CredentialDetailsFragment
import com.merative.healthpass.ui.mainActivity.FlavorVM
import com.merative.healthpass.ui.mainActivity.MainActivityVM
import com.merative.healthpass.utils.asyncToUiCompletable
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.functions.Consumer
import java.util.*
import javax.inject.Inject


abstract class BaseFragment : Fragment() {
    protected open val homeBtnEnabled = true

    private lateinit var loadingDialog: LoadingDialog

    @Inject
    protected lateinit var viewModelFactory: ViewModelProvider.Factory
    protected val mainActivityVM: MainActivityVM by activityViewModels { viewModelFactory }

    protected val flavorVM: FlavorVM by activityViewModels { viewModelFactory }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().applicationContext as App).appComponent
            .inject(this)

        loadingDialog = LoadingDialog(activity)
        lifecycle.addObserver(loadingDialog)
        logd("----- " + this::class.java.simpleName)
    }

    protected fun <VM : ViewModel> createViewModel(modelClass: Class<VM>): VM {
        return ViewModelProvider(this, viewModelFactory).get(modelClass)
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        showActionBar()
        enableBackButton()
    }

    /**
     * @param title set the action bar title
     */
    protected fun setTitle(title: CharSequence) {
        applySupportActionBar {
            setTitle(title)
        }
    }

    //region handle errors
    @CheckResult
    protected fun errorConsumer(message: String = ""): Consumer<Throwable> {
        return Consumer<Throwable> {
            loge(message, it)
            showLoading(false)
            handleCommonErrors(it)
        }
    }
    //endregion

    protected fun showLoading(show: Boolean) {
        loadingDialog.showLoading(show)
    }

    protected fun goToWallet() {
        findNavController().navigate(R.id.global_action_pop_to_home)
    }

    protected fun findActivityNavController(): NavController? {
        return activity?.findNavController(R.id.nav_host_fragment)
    }

    //region action bar
    /**
     * use it in onResume function
     */
    open fun enableBackButton(finishOnBackPressed: Boolean = false) {
        applySupportActionBar {
            setBackgroundDrawable(null)
            setHomeButtonEnabled(homeBtnEnabled)
            setDisplayHomeAsUpEnabled(homeBtnEnabled)
            setDisplayShowHomeEnabled(homeBtnEnabled)
            setHomeActionContentDescription(R.string.button_title_back)
            if (homeBtnEnabled) {
                setHomeAsUpIndicator(R.drawable.ic_arrow_back_blue_24dp)
            }
            if (finishOnBackPressed) {
                addBackDispatcher()
            }
        }
    }

    protected fun addBackDispatcher() {
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    closeActivity()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    protected open fun closeActivity() {
        showLoading(false)
        activity?.finish()
    }
    //endregion

    //region Share Cred
    protected fun showShareCredentialDialog(cp: Package) {
        activity?.showInputDialog(
            getString(R.string.share_cred_file_dialog_title),
            getString(R.string.share_cred_file_hint),
            getString(R.string.share_button), { _, text ->
                exportCredentialToFile(text, cp)
            },
            getString(R.string.button_title_cancel), {}, {},
            true
        )
    }

    private fun exportCredentialToFile(text: String, cp: Package) {
        showLoading(true)
        Completable.fromCallable {
            shareFile(
                cp.verifiableObject.credential!!.toFile(requireActivity(), text),
                CredentialDetailsFragment.REQUEST_CODE_SHARE,
                FILE_TYPE_JSON
            )
        }.asyncToUiCompletable()
            .subscribe({
                showLoading(false)
            }, errorConsumer())
    }
    //endregion

    override fun onDestroy() {
        super.onDestroy()
        showLoading(false)
    }

    //To Play the ring
    open fun playRing(isSuccess: Boolean) {
        val soundPool: SoundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder()
                .setMaxStreams(NUMBER_OF_SIMULTANEOUS_SOUNDS)
                .build()
        } else {
            // Deprecated way of creating a SoundPool before Android API 21.
            SoundPool(NUMBER_OF_SIMULTANEOUS_SOUNDS, AudioManager.STREAM_MUSIC, 0)
        }
        val soundId = soundPool.load(context, if (isSuccess) R.raw.jbl_confirm else R.raw.jbl_ambiguous, 1)
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                timer.cancel()
                soundPool.play(soundId, LEFT_VOLUME_VALUE, RIGHT_VOLUME_VALUE, SOUND_PLAY_PRIORITY, MUSIC_LOOP, PLAY_RATE)
            }
        }, SOUND_DELAY_IN_MILLIS)
    }

    companion object {
        const val KEY_IS_POPPED = "isPopped"
        const val KEY_FRAG_DATA = "frag_data"
        const val NUMBER_OF_SIMULTANEOUS_SOUNDS = 5
        const val LEFT_VOLUME_VALUE = 1.0f
        const val RIGHT_VOLUME_VALUE = 1.0f
        const val MUSIC_LOOP = 0
        const val SOUND_PLAY_PRIORITY = 0
        const val PLAY_RATE = 1.0f
        const val SOUND_DELAY_IN_MILLIS = 300L
    }
}