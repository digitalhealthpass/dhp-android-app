package com.merative.healthpass.common.dagger

import androidx.lifecycle.ViewModel
import com.merative.healthpass.ui.contactDetails.associatedData.AssociatedDataVM
import com.merative.healthpass.ui.contactDetails.asymmetricKey.AsymmetricKeyVM
import com.merative.healthpass.ui.contactDetails.availableCred.SelectCredentialsVM
import com.merative.healthpass.ui.contactDetails.consent.ConsentVM
import com.merative.healthpass.ui.contactDetails.details.ContactDetailsVM
import com.merative.healthpass.ui.contactDetails.revoke.ConsentRevokeVM
import com.merative.healthpass.ui.contactDetails.uploadComplete.UploadCompleteVM
import com.merative.healthpass.ui.credential.selectConnection.SelectConnectionsVM
import com.merative.healthpass.ui.home.HomeViewModel
import com.merative.healthpass.ui.registration.details.RegistrationDetailsVM
import com.merative.healthpass.ui.registration.organization.OrgViewModel
import com.merative.healthpass.ui.registration.registerCode.RegistrationCodeVM
import com.merative.healthpass.ui.registration.selection.FieldSelectionVM
import com.merative.healthpass.ui.registration.userAgreement.UserAgreementVM
import com.merative.healthpass.ui.registration.verification.VerificationViewModel
import com.merative.healthpass.ui.scanChooser.ScanChooserVM
import com.merative.healthpass.ui.settings.SettingsViewModel
import com.merative.healthpass.ui.settings.reset.ResetCacheViewModel
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
    @ViewModelKey(ScanChooserVM::class)
    abstract fun scanChooserViewModel(viewModel: ScanChooserVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(OrgViewModel::class)
    abstract fun orgViewModel(viewModel: OrgViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RegistrationCodeVM::class)
    abstract fun regCodeViewModel(viewModel: RegistrationCodeVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(VerificationViewModel::class)
    abstract fun verificationCodeViewModel(viewModel: VerificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserAgreementVM::class)
    abstract fun userAgreementViewModel(viewModel: UserAgreementVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(RegistrationDetailsVM::class)
    abstract fun regDetailsViewModel(viewModel: RegistrationDetailsVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(FieldSelectionVM::class)
    abstract fun fieldSelectionVM(viewModel: FieldSelectionVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ResetCacheViewModel::class)
    abstract fun resetCacheViewModel(viewModel: ResetCacheViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun settingsViewModel(viewModel: SettingsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SelectCredentialsVM::class)
    abstract fun credentialsViewModel(viewModel: SelectCredentialsVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConsentVM::class)
    abstract fun consentViewModel(viewModel: ConsentVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UploadCompleteVM::class)
    abstract fun uploadCompleteViewModel(viewModel: UploadCompleteVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ContactDetailsVM::class)
    abstract fun contactDetailsViewModel(viewModel: ContactDetailsVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AsymmetricKeyVM::class)
    abstract fun contactAssociatedKeyViewModel(viewModel: AsymmetricKeyVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AssociatedDataVM::class)
    abstract fun contactAssociatedDataViewModel(viewModel: AssociatedDataVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConsentRevokeVM::class)
    abstract fun consentRevokeVM(viewModel: ConsentRevokeVM): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SelectConnectionsVM::class)
    abstract fun selectConnectionsVM(viewModel: SelectConnectionsVM): ViewModel
}