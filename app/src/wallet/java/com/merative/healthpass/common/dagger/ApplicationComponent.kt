package com.merative.healthpass.common.dagger

import com.merative.healthpass.network.dagger.*
import com.merative.healthpass.ui.common.baseViews.BaseActivity
import com.merative.healthpass.ui.common.baseViews.BaseBottomSheet
import com.merative.healthpass.ui.common.baseViews.BaseFragment
import com.merative.healthpass.ui.common.baseViews.BaseListBottomSheet
import com.merative.healthpass.ui.debug.debugFragment.DebugFragment
import com.merative.healthpass.ui.home.HomeFragment
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.ui.scanChooser.ScanChooserFragment
import com.merative.healthpass.utils.Utils
import com.merative.healthpass.utils.pref.DBMigration
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.healthpass.utils.pref.SharedPrefUtils
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        DBModule::class,
        ViewModelModule::class,
        NetworkModule::class,
        BuildTypeModule::class,
        FlavorModule::class,
        SchemaModule::class,
        RevocationModule::class,
        HitRegistrationModule::class,
        NihRegistrationModule::class,
        NclRegistrationModule::class,
        SamsungModule::class
    ]
)
interface ApplicationComponent {

    fun environmentHandler(): EnvironmentHandler
    fun sharePrefUtils(): SharedPrefUtils
    fun packageDB(): PackageDB
    fun dbMigration(): DBMigration
    fun utils(): Utils

    // This tells Dagger that Class requests injection so the graph needs to
    // satisfy all the dependencies of the fields that Class is requesting.
    fun inject(baseActivity: BaseActivity)

    fun inject(baseFragment: BaseFragment)
    fun inject(baseFragment: BaseBottomSheet)
    fun inject(fragment: BaseListBottomSheet)
    fun inject(fragment: DebugFragment)//keep this since Debug fragment is not extending BaseFragment

    fun inject(homeFragment: HomeFragment)

    fun inject(baseFragment: ScanChooserFragment)
}