package com.merative.healthpass.models.region

import android.content.res.Resources
import com.merative.healthpass.R
import com.merative.healthpass.extensions.isNotNullOrEmpty
import java.io.Serializable

enum class Env(
    val url: String,
    val issuerId: String,
    val title: String? = null,
    val description: String? = null,
    val titleRes: Int? = null,
    val descriptionRes: Int? = null,
    val isProd: Boolean = false,
    val SAMSUNG_PAY_BASE_URL: String = "https://api-us1.mpay.samsung.com"
) : Serializable {
    LOCALHOST(
        "http://192.168.1.12:3000/api/v1/",
        "hpass.issuer1",
        "localhost",
        ""
    ),
    AUSTRALIA_SANDBOX(
        "https://hpass-api-edge-dhp-hpass.openshift-ibp-poc-us-ea-4b0b005ca0769ba035219b04895f6ade-0000.us-east.containers.appdomain.cloud/api/v1",
        "hpass.issuer1",
        "Australia Sandbox 1",
        ""
    ),
    USA_SANDBOX_1(
        "https://sandbox1.wh-hpass.dev.acme.com/api/v1/",
        "hpass.issuer1",
        "USA Sandbox 1",
    ),
    USA_SANDBOX_2(
        "https://sandbox2.wh-hpass.dev.acme.com/api/v1/",
        "hpass.issuer1",
        "USA Sandbox 2",
    ),
    USA_DEV_1(
        "https://dev1.wh-hpass.dev.acme.com/api/v1/",
        "hpass.issuer1",
        "USA Dev 1",
    ),
    USA_DEV_2(
        "https://dev2.wh-hpass.dev.acme.com/api/v1/",
        "hpass.issuer1",
        "USA Dev 2",
    ),
    USA_DEV_3(
        "https://dev3.wh-hpass.dev.acme.com/api/v1/",
        "hpass.issuer1",
        "USA Dev 3",
    ),
    USA_QA(
        "https://release1.wh-hpass.dev.acme.com/api/v1/",
        "hpass.issuer1",
        "USA QA",
    ),
    USA_STAGE_2(
        "https://staging02.wh-hpass.test.acme.com/api/v1/",
        "hpass.issuer1",
        "USA Stage 2",
    ),
    EMEA_QA(
        "https://release3.wh-hpass.dev.acme.com/api/v1/",
        "hpass.issuer1",
        "EMEA QA",
    ),
    EMEA_QA_2(
        "https://release4.wh-hpass.dev.acme.com/api/v1/",
        "hpass.issuer1",
        "EMEA QA 2",
    ),
    USA(
        "https://healthpass01.wh-hpass.acme.com/api/v1/",
        "hpass.issuer1",
        null,
        null,
        R.string.region_usa,
        null,
        true
    ),
    ASIA(
        "https://healthpass01.wh-hpass.acme.com/api/v1/",
        "hpass.issuer1",
        null,
        null,
        R.string.region_asia_pacific,
        R.string.region_asia_pacific_description,
        true
    ),
    EMEA(
        "https://healthpass01.wh-hpass.acme.com/api/v1/",
        "hpass.issuer1",
        null,
        null,
        R.string.region_emea,
        R.string.region_emea_description,
        true
    ),

    //    EMEA(
//        "https://healthpass04-eu.wh-hpass.acme.com/api/v1/",
//        "hpass.issuer1",
//        null,
//        null,
//        R.string.region_emea,
//        R.string.region_emea_description,
//        true
//    )
    CANADA(
        "https://healthpass01.wh-hpass.acme.com/api/v1/",
        "hpass.issuer1",
        null,
        "",
        R.string.region_canada,
        null,
        true
    ),
//   CANADA(
//        "https://healthpass03-tor.wh-hpass.acme.com/api/v1/",
//        "hpass.issuer1",
//        "Canada",
//        "",
//        null,
//        null,
//        true
//    ),
}

fun Env.getEnvironmentTitle(resources: Resources) = when {
    this.title.isNotNullOrEmpty() -> this.title!!
    this.titleRes != null -> resources.getString(this.titleRes)
    else -> ""
}