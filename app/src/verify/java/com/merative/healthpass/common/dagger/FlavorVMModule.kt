package com.merative.healthpass.common.dagger

import androidx.lifecycle.ViewModel
import com.merative.healthpass.ui.home.HomeViewModel
import com.merative.healthpass.ui.organization.OrganizationDetailsVM
import com.merative.healthpass.ui.organizations_list.OrganizationsListViewModel
import com.merative.healthpass.ui.results.ScanResultsViewModel
import com.merative.healthpass.ui.scanner_permissions.ScannerPermissionsVM
import com.merative.healthpass.ui.settings.SettingsViewModel
import com.merative.healthpass.ui.settings.kiosk.KioskSettingsViewModel
import com.merative.healthpass.ui.settings.reset.ResetCacheViewModel
import com.merative.healthpass.ui.settings.sound.SoundVibrationVM
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class FlavorVMModule {

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun homeViewModel(viewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScannerPermissionsVM::class)
    abstract fun scannerPermissionsViewModel(viewModel: ScannerPermissionsVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ScanResultsViewModel::class)
    abstract fun scanResultsModel(viewViewModel: ScanResultsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OrganizationDetailsVM::class)
    abstract fun organizationDetailsViewModel(viewModel: OrganizationDetailsVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OrganizationsListViewModel::class)
    abstract fun organizationsListViewModel(viewModel: OrganizationsListViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun settingsViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ResetCacheViewModel::class)
    abstract fun resetCacheViewModel(viewModel: ResetCacheViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(KioskSettingsViewModel::class)
    abstract fun resetKioskSettingsViewModel(viewModel: KioskSettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SoundVibrationVM::class)
    abstract fun soundSettingsViewModel(viewModel: SoundVibrationVM): ViewModel
}