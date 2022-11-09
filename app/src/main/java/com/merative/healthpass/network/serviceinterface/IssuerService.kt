package com.merative.healthpass.network.serviceinterface

import com.merative.healthpass.models.api.IssuerResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IssuerService {

    @GET("hpass/issuers")
    fun getIdhpGhpVcIssuers(): Single<Response<IssuerResponse>>

    @GET("hpass/generic-issuers/dcc")
    fun getDccIssuers(
        @Query("pagesize") pageSize: Int,
        @Query("bookmark") bookmark: String? = null
    ): Single<Response<IssuerResponse>>

    @GET("hpass/generic-issuers/vci")
    fun getShcIssuers(
        @Query("pagesize") pageSize: Int,
        @Query("bookmark") bookmark: String? = null
    ): Single<Response<IssuerResponse>>

    @GET("hpass/issuers/{did}")
    fun getIdhpGhpVcIssuersByIssuerId(
        @Path("did", encoded = true) did: String,
    ): Single<Response<IssuerResponse>>

    @GET("hpass/generic-issuers/dcc")
    //@GET("hpass/generic-issuers/dcc?id=FhciF/j3plg=")
    fun getDccIssuersByKeyId(
        @Query("kid", encoded = true) id: String,
        @Query("pagesize") pageSize: Int,
        @Query("bookmark") bookmark: String = ""
    ): Single<Response<IssuerResponse>>
}