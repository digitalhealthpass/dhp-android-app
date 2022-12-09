package com.merative.healthpass.models.api

import android.os.Parcelable
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import com.merative.healthpass.models.specificationconfiguration.DisabledRules
import com.merative.healthpass.models.specificationconfiguration.DisabledSpecificationConfiguration
import com.merative.healthpass.models.specificationconfiguration.SpecificationConfiguration
import com.merative.healthpass.models.specificationconfiguration.ValueSets
import com.merative.watson.healthpass.verifiablecredential.models.parcel.JsonElementParceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import java.util.*

@Parcelize
data class ConfigResponse(
    val type: String?,
    val payload: ArrayList<ConfigPayload> = arrayListOf(),
    var saveTime: Date = Date(),
) : BaseResponse()

@Parcelize
data class ConfigPayload(
    var id: String?,
    @SerializedName("created_by")
    var createdBy: String?,
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("updated_at")
    var updatedAt: String?,
    var version: String?,
    var name: String?,
    var customer: String?,
    var customerId: String?,
    var organization: String?,
    var organizationId: String?,
    var label: String?,
    var offline: Boolean?,
    var refresh: Int?,
    var verifierType: String?,
    var configuration: @WriteWith<JsonElementParceler> JsonElement?,
    var masterCatalog: Boolean? = null,
    var specificationConfigurations: ArrayList<SpecificationConfiguration> = arrayListOf(),
    var valueSets: ArrayList<ValueSets> = arrayListOf(),
    var disabledSpecifications: ArrayList<DisabledSpecificationConfiguration> = arrayListOf(),
    var disabledRules: ArrayList<DisabledRules> = arrayListOf(),
) : Parcelable