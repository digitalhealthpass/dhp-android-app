package com.merative.healthpass.models.results

import com.merative.healthpass.models.specificationconfiguration.SpecificationConfiguration

data class ResultsModel(
    val displayList: List<Pair<String, String>>,
    val issuerName: String?,
    var specificationConfiguration: SpecificationConfiguration?,
    val status: Boolean,
    val throwable: Throwable
)
