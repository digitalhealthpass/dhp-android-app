package com.merative.healthpass.models.api.registration.uploadCredential

import android.os.Parcelable
import com.merative.healthpass.models.api.BaseResponse
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class SubmitDataResponse(val payload: SubmitDataPayload) : BaseResponse(), Parcelable

@Parcelize
data class SubmitDataPayload(
    val credentialsProcessed: List<CredentialProcessed>,
    val credentialsNotProcessed: List<CredentialNotProcessed>
) : Parcelable

@Parcelize
data class CredentialProcessed(val credentialType: String, val credentialId: @RawValue String) : Parcelable

@Parcelize
data class CredentialNotProcessed(
    val credentialType: String,
    val credentialId: @RawValue String,
    val reason: String
) : Parcelable