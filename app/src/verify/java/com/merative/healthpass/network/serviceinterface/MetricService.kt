package com.merative.healthpass.network.serviceinterface

import com.merative.healthpass.models.api.metric.SubmitMetricsRequest
import com.merative.healthpass.models.api.metric.SubmitMetricsResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

@JvmSuppressWildcards
interface MetricService {
    @POST("metering/metrics/verifier/batch")
    fun submitMetrics(
        @Body body: SubmitMetricsRequest,
    ): Single<Response<SubmitMetricsResponse>>
}