package com.merative.healthpass.models.api.registration.nih

import com.merative.healthpass.models.api.BaseResponse
import com.merative.watson.healthpass.verifiablecredential.models.credential.Credential

data class OnBoardingResponse(val payload: List<Credential>) : BaseResponse()
