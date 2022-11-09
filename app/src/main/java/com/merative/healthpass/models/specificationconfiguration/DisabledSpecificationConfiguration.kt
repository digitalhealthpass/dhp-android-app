package com.merative.healthpass.models.specificationconfiguration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class DisabledSpecificationConfiguration(
    var id: String? = null,
    var name: String? = null,
    var description: String? = null,
    var credentialSpec: String? = null,
    var credentialCategory: String? = null,
    var classifierRule: ClassifierRule? = ClassifierRule(),
    var metrics: ArrayList<Metrics> = arrayListOf(),
    var display: ArrayList<Display> = arrayListOf(),
    var rules: ArrayList<Rules> = arrayListOf(),
    var trustLists: ArrayList<TrustLists> = arrayListOf(),
) : Parcelable