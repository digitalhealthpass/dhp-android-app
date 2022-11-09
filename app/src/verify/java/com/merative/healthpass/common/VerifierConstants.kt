package com.merative.healthpass.common

import com.merative.healthpass.extensions.IS_QA

object VerifierConstants {
    //SCH Signature Verification Endpoint
    const val VCI_ISSUER_ENDPOINT = "/.well-known/jwks.json"

    //DCC response json location
    const val DCC_PATH_LOCATION: String = "issuer-key-type-dcc.json"

    //Display Set json location
    const val DISPLAY_SET_PATH_LOCATION: String = "display-set.json"

    val MetricsUploadLimitCount: Int
        get() {
            return if (IS_QA) 5
            else return 100
        }

    val MetricsUploadDeleteCount: Int
        get() {
            return if (IS_QA) 15
            else 1000
        }
}