package com.merative.healthpass.ui.contactDetails.uploadComplete

import com.merative.healthpass.extensions.toHashMapWithValue
import com.merative.healthpass.models.api.registration.uploadCredential.SubmitDataPayload
import com.merative.healthpass.models.sharedPref.Package
import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.watson.healthpass.verifiablecredential.models.cose.cwt
import javax.inject.Inject

class UploadCompleteVM @Inject constructor(
) : BaseViewModel() {

    fun filterList(
        selectedCredentials: ArrayList<Package>,
        submitDataPayload: SubmitDataPayload?
    ): Pair<ArrayList<Package>, ArrayList<Package>> {
        val uploadedList = ArrayList<Package>()
        val failedList = ArrayList<Package>()

        val sharedMap =
            submitDataPayload?.credentialsProcessed.toHashMapWithValue { it.credentialId }
        val failedMap =
            submitDataPayload?.credentialsNotProcessed.toHashMapWithValue { it.credentialId }

        selectedCredentials.forEach { pack ->
            when {
                //Verifying IDHP
                pack.verifiableObject.credential?.id != null -> {
                    if (sharedMap.containsKey(pack.verifiableObject.credential?.id)) {
                        uploadedList.add(pack)
                    }
                    if (failedMap.containsKey(pack.verifiableObject.credential?.id)) {
                        failedList.add(pack)
                    }
                }
                //Verifying DCC
                pack.verifiableObject.cose?.cwt != null -> {
                    pack.verifiableObject.cose?.cwt?.certificate?.vaccinations?.forEach {
                        it.certificateIdentifier.let { id ->
                            if (sharedMap.containsKey(id)) {
                                uploadedList.add(pack)
                            }
                            if (failedMap.containsKey(id)) {
                                failedList.add(pack)
                            }
                        }
                    }
                }
                else -> {
                    sharedMap.forEach {
                        try {
                            it.key.toDouble().let { d ->
                                if (d == pack.verifiableObject.jws?.payload?.nbf) {
                                    uploadedList.add(pack)
                                }
                            }
                        } catch (ex: Exception) {
                        }
                    }
                    failedMap.forEach {
                        try {
                            it.key.toDouble().let { d ->
                                if (d == pack.verifiableObject.jws?.payload?.nbf) {
                                    failedList.add(pack)
                                }
                            }
                        } catch (ex: Exception) {
                        }
                    }
                }
            }
        }

        return uploadedList to failedList
    }
}