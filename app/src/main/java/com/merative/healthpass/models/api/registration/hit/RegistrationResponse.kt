package com.merative.healthpass.models.api.registration.hit

import android.os.Parcelable
import com.merative.healthpass.models.api.BaseResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class RegistrationResponse(
    var type: String?,
    var payload: RegistrationPayLoad?
) : BaseResponse(), Parcelable