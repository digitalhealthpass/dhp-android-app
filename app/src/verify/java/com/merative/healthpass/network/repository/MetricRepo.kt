package com.merative.healthpass.network.repository

import com.merative.healthpass.models.Metric
import com.merative.healthpass.models.api.metric.ScanObject
import com.merative.healthpass.models.api.metric.SubmitMetricObject
import com.merative.healthpass.models.api.metric.SubmitMetricsRequest
import com.merative.healthpass.models.api.metric.SubmitMetricsResponse
import com.merative.healthpass.network.serviceinterface.MetricService
import com.merative.healthpass.utils.RxHelper
import com.merative.healthpass.utils.asyncToUiSingle
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import javax.inject.Inject

class MetricRepo @Inject constructor(
    private val metricService: MetricService
) {
    fun submitMetrics(metrics: List<Metric>): Single<Response<SubmitMetricsResponse>> {
        return Single.fromCallable {
            metrics.filter {
                !it.timestamp.isNullOrEmpty() &&
                        !it.status.isNullOrEmpty() &&
                        !it.credentialSpec.isNullOrEmpty() &&
                        !it.customerId.isNullOrEmpty() &&
                        !it.organizationId.isNullOrEmpty() &&
                        !it.verifierId.isNullOrEmpty()
            }.map { metric ->
                val count = metrics.filter {
                    it.verifierId == metric.verifierId &&
                            it.organizationId == metric.organizationId &&
                            it.customerId == metric.customerId &&
                            it.issuerId == metric.issuerId &&
                            it.type == metric.type &&
                            it.status == metric.status &&
                            it.timestamp == metric.timestamp
                }.size
                val scans = ScanObject(
                    metric.issuerId,
                    metric.issuerName,
                    metric.credentialSpec,
                    metric.type,
                    metric.timestamp,
                    metric.status,
                    count
                )
                SubmitMetricObject(
                    metric.organizationId,
                    metric.verifierId,
                    metric.customerId,
                    listOf(scans)
                )
            }
        }.flatMap {
            val body = SubmitMetricsRequest(it)
            metricService.submitMetrics(body)
                .onErrorResumeNext { t ->
                    RxHelper.handleErrorSingle(t)
                }
        }.asyncToUiSingle()
    }
}