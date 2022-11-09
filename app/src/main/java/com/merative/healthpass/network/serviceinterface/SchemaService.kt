package com.merative.healthpass.network.serviceinterface

import com.merative.healthpass.models.api.metadata.MetaResponse
import com.merative.healthpass.models.api.schema.SchemaResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface SchemaService {

    @GET("hpass/schemas/{did}")
    fun getSchema(
        @Path("did", encoded = false) did: String,
    ): Single<Response<SchemaResponse>>

    @GET("hpass/issuers/{issuerId}/metadata")
    fun requestMetadata(
        @Path("issuerId", encoded = false) issuerId: String,
    ): Single<Response<MetaResponse>>
}