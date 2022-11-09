package com.merative.healthpass.network.serviceinterface

import com.merative.healthpass.models.api.ConfigResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ConfigService {

    @GET("verifier/config/api/v1/verifier-configurations/{id}/{version}/content")
    fun getConfig(
        @Path("id") id: String,
        @Path("version") version: String = "latest",
    ): Single<Response<ConfigResponse>>
}