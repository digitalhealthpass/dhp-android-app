package com.merative.healthpass.models

import retrofit2.HttpException
import retrofit2.Response

class OfflineModeException(response: Response<*>) : HttpException(response)