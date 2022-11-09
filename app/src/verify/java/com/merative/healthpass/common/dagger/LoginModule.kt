package com.merative.healthpass.common.dagger

import com.merative.healthpass.network.qualifier.LoginApi
import com.merative.healthpass.network.repository.LoginRepo
import com.merative.healthpass.network.serviceinterface.LoginService
import com.merative.healthpass.utils.pref.SharedPrefUtils
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class LoginModule {
    @Provides
    fun provideLoginRepo(
        loginService: LoginService,
        sharedPrefUtils: SharedPrefUtils,
    ): LoginRepo {
        return LoginRepo(loginService, sharedPrefUtils)
    }

    @Provides
    fun provideLoginService(@LoginApi retrofit: Retrofit): LoginService {
        return retrofit
            .create(LoginService::class.java)
    }
}