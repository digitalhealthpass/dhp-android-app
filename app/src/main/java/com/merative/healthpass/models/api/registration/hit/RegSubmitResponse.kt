package com.merative.healthpass.models.api.registration.hit

import com.merative.healthpass.models.api.BaseResponse
import com.merative.watson.healthpass.verifiablecredential.models.credential.Credential

data class RegSubmitResponse(val payload: List<Credential>) : BaseResponse()
