package com.merative.healthpass.network.dagger

import com.merative.healthpass.BuildConfig
import com.merative.healthpass.network.interceptors.HeadersInterceptor
import com.merative.healthpass.network.interceptors.LoginInterceptor
import com.merative.healthpass.network.qualifier.LoginApi
import com.merative.healthpass.network.qualifier.RegularApi
import com.merative.healthpass.network.qualifier.SPayApi
import com.merative.watson.healthpass.verifiablecredential.utils.GsonHelper
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.pref.PackageDB
import com.merative.healthpass.utils.pref.SharedPrefUtils
import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

@Module
class NetworkModule {

    @Provides
    @RegularApi
    fun provideRetrofit(
        environmentHandler: EnvironmentHandler,
        @RegularApi httpBuilder: OkHttpClient.Builder
    ): Retrofit {
        val client = httpBuilder
            .build()

        return Retrofit.Builder()
            .baseUrl(environmentHandler.currentEnv.url)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonHelper.gson))
            .client(client)
            .build()
    }

    @Provides
    @SPayApi
    fun provideSamsungRetrofit(
        environmentHandler: EnvironmentHandler,
        @RegularApi httpBuilder: OkHttpClient.Builder
    ): Retrofit {
        val client = httpBuilder
            .build()

        return Retrofit.Builder()
            .baseUrl(environmentHandler.currentEnv.SAMSUNG_PAY_BASE_URL)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonHelper.gson))
            .client(client)
            .build()
    }

    @Provides
    @LoginApi
    fun provideLoginRetrofit(
        environmentHandler: EnvironmentHandler,
        @LoginApi httpBuilder: OkHttpClient.Builder
    ): Retrofit {
        val client = httpBuilder
            .build()

        return Retrofit.Builder()
            .baseUrl(environmentHandler.currentEnv.url)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonHelper.gson))
            .client(client)
            .build()
    }

    @Provides
    @RegularApi
    fun provideOkHttp(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        headersInterceptor: HeadersInterceptor,
        loginInterceptor: LoginInterceptor,
        tls12ConnectionSpec: ConnectionSpec?,
    ): OkHttpClient.Builder {

        val httpClient = OkHttpClient.Builder()

        if (tls12ConnectionSpec != null) {
            httpClient.connectionSpecs(listOf(tls12ConnectionSpec))
        }

        //order is important, to make sure the user is logged in
        httpClient.addInterceptor(loginInterceptor)

        httpClient.addInterceptor(headersInterceptor)

        if (BuildConfig.DEBUG) {
            httpClient.addInterceptor(httpLoggingInterceptor)
        }

        return httpClient
    }

    @Provides
    @LoginApi
    fun provideLoginOkHttp(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        headersInterceptor: HeadersInterceptor,
        tls12ConnectionSpec: ConnectionSpec?,
    ): OkHttpClient.Builder {

        val httpClient = OkHttpClient.Builder()

        if (tls12ConnectionSpec != null) {
            httpClient.connectionSpecs(listOf(tls12ConnectionSpec))
        }

        httpClient.addInterceptor(headersInterceptor)

        if (BuildConfig.DEBUG) {
            httpClient.addInterceptor(httpLoggingInterceptor)
        }

        return httpClient
    }

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return interceptor
    }

    @Provides
    fun provideLoginInterceptor(
        sharedPrefUtils: SharedPrefUtils,
        environmentHandler: EnvironmentHandler,
        packageDB: PackageDB
    ): LoginInterceptor {
        return LoginInterceptor(sharedPrefUtils, environmentHandler, packageDB)
    }

    @Provides
    fun provideHeadersInterceptor(
        environmentHandler: EnvironmentHandler,
        sharedPrefUtils: SharedPrefUtils
    ): HeadersInterceptor {
        return HeadersInterceptor(environmentHandler, sharedPrefUtils)
    }
}