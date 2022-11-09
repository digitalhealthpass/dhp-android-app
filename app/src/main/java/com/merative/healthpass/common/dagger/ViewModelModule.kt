package com.merative.healthpass.common.dagger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.merative.healthpass.ui.credential.details.CredentialDetailsVM
import com.merative.healthpass.ui.credential.sharing.ObfuscationSettingsVM
import com.merative.healthpass.ui.debug.debugFragment.DebugVM
import com.merative.healthpass.ui.landing.LandingViewModel
import com.merative.healthpass.ui.mainActivity.FlavorVM
import com.merative.healthpass.ui.mainActivity.MainActivityVM
import com.merative.healthpass.ui.pin.PinViewModel
import com.merative.healthpass.ui.privacy.PrivacyPolicyVM
import com.merative.healthpass.ui.region.RegionSelectionVM
import com.merative.healthpass.ui.scanVerify.ScanVerifyVM
import com.merative.healthpass.ui.settings.pinSettings.PinSettingsViewModel
import com.merative.healthpass.ui.terms.TermsViewModel
import com.merative.healthpass.ui.tutorial.TutorialVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

/**
 * Add the view models here or its parent to allow the factory to create it
 */
@Module
abstract class ViewModelModule : FlavorVMModule() {
    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(LandingViewModel::class)
    abstract fun landingViewModel(viewModel: LandingViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScanVerifyVM::class)
    abstract fun scanVerifyViewModel(viewModel: ScanVerifyVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DebugVM::class)
    abstract fun debugViewModel(viewModel: DebugVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TermsViewModel::class)
    abstract fun termsViewModel(viewModel: TermsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PrivacyPolicyVM::class)
    abstract fun privacyViewModel(viewModel: PrivacyPolicyVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CredentialDetailsVM::class)
    abstract fun credentialDetailsViewModel(viewModel: CredentialDetailsVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TutorialVM::class)
    abstract fun tutorialViewModel(viewModel: TutorialVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PinViewModel::class)
    abstract fun pinViewModel(viewModel: PinViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(PinSettingsViewModel::class)
    abstract fun pinSettingsViewModel(viewModel: PinSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ObfuscationSettingsVM::class)
    abstract fun obfuscationSettingsVM(viewModel: ObfuscationSettingsVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RegionSelectionVM::class)
    abstract fun regionSelectionVM(viewModel: RegionSelectionVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityVM::class)
    abstract fun mainActivityVM(viewModel: MainActivityVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FlavorVM::class)
    abstract fun flavorViewModel(viewModel: FlavorVM): ViewModel
}