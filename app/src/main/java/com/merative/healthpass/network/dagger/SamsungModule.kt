package com.merative.healthpass.network.dagger

import com.merative.healthpass.network.qualifier.SPayApi
import com.merative.healthpass.network.repository.SamsungRepo
import com.merative.healthpass.network.serviceinterface.DeviceAvailableService
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class SamsungModule {

    @Provides
    fun provideSamsungRepo(service: DeviceAvailableService): SamsungRepo {
        return SamsungRepo(service)
    }

    @Provides
    fun provideDeviceAvailableService(@SPayApi retrofit: Retrofit): DeviceAvailableService {
        return retrofit
            .create(DeviceAvailableService::class.java)
    }
}