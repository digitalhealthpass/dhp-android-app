package com.merative.healthpass.network.repository

import com.merative.healthpass.extensions.isSuccessfulAndHasBody
import com.merative.healthpass.network.serviceinterface.RevocationService
import com.merative.watson.healthpass.verifiablecredential.extensions.asJsonObjectOrNull
import com.merative.watson.healthpass.verifiablecredential.extensions.getBooleanOrNull
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class RevocationRepo @Inject constructor(
    private val revocationService: RevocationService
) {

    fun getRevokeStatus(did: String): Single<Boolean> {
        return revocationService.getRevokeStatus(did).map {
            if (!it.isSuccessfulAndHasBody()) {
                true
            } else {
                val exists =
                    it.body()?.payload.asJsonObjectOrNull()?.getBooleanOrNull("exists") ?: false
                !exists
            }
        }
    }
}