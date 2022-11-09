package com.merative.healthpass.models.specificationconfiguration

import android.os.Parcelable
import com.merative.healthpass.models.results.VerificationResult
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class SpecificationConfiguration(
    var id: String? = null,
    var name: String? = null,
    var description: String? = null,
    var version: String? = null,
    var credentialSpecDisplayValue: String? = null,
    var credentialCategoryDisplayValue: String? = null,
    var credentialSpec: String? = null,
    var credentialCategory: String? = null,
    var classifierRule: ClassifierRule? = ClassifierRule(),
    var metrics: ArrayList<Metrics> = arrayListOf(),
    var display: ArrayList<Display> = arrayListOf(),
    var rules: ArrayList<Rules> = arrayListOf(),
    var trustLists: ArrayList<TrustLists> = arrayListOf(),
    var verificationResult: @RawValue VerificationResult? = null
) : Parcelable