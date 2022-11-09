package com.merative.healthpass.network.serviceinterface

import com.merative.healthpass.models.RevocationStatusResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface RevocationService {

    @GET("hpass/credentials/{id}/revoke_status/optional")
    fun getRevokeStatus(
        @Path("id") id: String
    ): Single<Response<RevocationStatusResponse>>
}