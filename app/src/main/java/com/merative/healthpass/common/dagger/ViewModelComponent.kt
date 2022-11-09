package com.merative.healthpass.common.dagger

import com.merative.healthpass.ui.landing.LandingViewModel
import dagger.Component

@Component(
    modules = arrayOf(
        ViewModelModule::class
    )
)
interface ViewModelComponent {
    // inject your view models
    fun inject(mainViewModel: LandingViewModel)

}