package com.merative.healthpass.models.specificationconfiguration

import com.google.gson.annotations.SerializedName

data class Payload(
    @SerializedName("id") var id: String? = null,
    @SerializedName("created_by") var createdBy: String? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    @SerializedName("version") var version: String? = null,
    @SerializedName("unrestricted") var unrestricted: Boolean? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("offline") var offline: Boolean? = null,
    @SerializedName("refresh") var refresh: Int? = null,
    @SerializedName("master-catalog") var masterCatalog: Boolean? = null,
    @SerializedName("specification-configurations") var specificationConfigurations: ArrayList<SpecificationConfiguration> = arrayListOf(),
  )