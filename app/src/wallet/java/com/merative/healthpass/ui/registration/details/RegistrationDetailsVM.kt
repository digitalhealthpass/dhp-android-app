package com.merative.healthpass.ui.registration.details

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.merative.healthpass.extensions.isSuccessfulAndHasBody
import com.merative.healthpass.extensions.toArrayList
import com.merative.healthpass.extensions.toStringWithoutBrackets
import com.merative.healthpass.models.api.registration.nih.OnBoardingResponse
import com.merative.healthpass.models.api.schema.SchemaPayload
import com.merative.healthpass.models.api.schema.SchemaResponse
import com.merative.healthpass.models.credential.AsymmetricKey
import com.merative.healthpass.models.sharedPref.ContactPackage
import com.merative.healthpass.network.repository.NCLRegRepo
import com.merative.healthpass.network.repository.NIHRegRepo
import com.merative.healthpass.network.repository.SchemaRepo
import com.merative.healthpass.utils.schema.*
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
import retrofit2.Response
import javax.inject.Inject

class RegistrationDetailsVM @Inject constructor(
    private val schemaRepo: SchemaRepo,
    private val nihRegRepo: NIHRegRepo,
    private val nclRegRepo: NCLRegRepo,
) : ViewModel() {
    var schema: SchemaPayload? = null
        private set

    var filteredList = ArrayList<Field>()

    /** hold the new value that will be sent, and keep original values*/
    val fieldsMap = HashMap<Field, Any?>()
    val isFormValid: PublishSubject<Boolean> = PublishSubject.create()

    val asymmetricKey by lazy { AsymmetricKey.createKeyTest() }

    fun requestSchema(schemaId: String): Single<Response<SchemaResponse>> {
        return schemaRepo.fetchSchema(schemaId).doOnSuccess {
            if (it.isSuccessfulAndHasBody()) {
                schema = it.body()?.payload
            }
        }
    }

    fun getFieldFromJson(json: JsonObject?, orgName: String, code: String): ArrayList<Field> {
        if (filteredList.isEmpty()) {
            fieldsMap.clear()

            filteredList = SchemaProcessor().processRegJson(json).toArrayList()

            //adjust the Key field
            val idField =
                filteredList.find { it.path == Field.ID || it.path == Field.FIELD_NAME_PUBLIC_KEY }
            idField?.apply {
                editable = false
                value = asymmetricKey?.publicKey.orEmpty()
            }

            filteredList = filteredList.filter { it.visible }.toArrayList()

            val orgField = Field.create(Field.ORGANIZATION_CODE, orgName).apply {
                editable = false
                visible = false
            }

            val regCodeField = Field.create(Field.REGISTRATION_CODE, code).apply {
                editable = false
            }

            filteredList.forEach { field ->
                if (!field.isLocation() && field.isString()) {
                    field.editable = true
                }
                fieldsMap[field] = null
            }
            fieldsMap[orgField] = orgField.value
            fieldsMap[regCodeField] = regCodeField.value
            if (idField != null) {
                fieldsMap[idField] = asymmetricKey?.publicKey.orEmpty()
            }
        }
        return filteredList
    }

    fun getFieldFromSchema(orgName: String, code: String): ArrayList<Field> {
        if (filteredList.isEmpty() || this.schema != schema) {
            fieldsMap.clear()

            filteredList = SchemaProcessor().processSchema(schema)
                .filter { it.visible }
                .toArrayList()

            //adjust the Key field
            val keyField = filteredList.find { it.path == Field.KEY }
            keyField?.apply {
                editable = false
                value = asymmetricKey?.publicKey.orEmpty()
            }

            val idField = Field.create(Field.ID, orgName).apply {
                editable = false
            }

            val regCodeField = Field.create(Field.REGISTRATION_CODE, code).apply {
                visible = false
                editable = false
            }

            filteredList.forEach { field ->
                fieldsMap[field] = null
            }

            filteredList.add(idField)
            fieldsMap[idField] = idField.value

            filteredList.add(regCodeField)
            fieldsMap[regCodeField] = regCodeField.value

            if (keyField != null) {
                fieldsMap[keyField] = asymmetricKey?.publicKey.orEmpty()
            }
        }
        return filteredList
    }

    fun validateForm() {
        var isValid = true
        //disabling the check to align with iOS and till backend start sending "required" for all organizations
//        fieldsMap.forEach { (key, value) ->
//            if (key.required) {
//                if (value is CharSequence && value.isNullOrEmpty()) {
//                    isValid = false
//                    return@forEach
//                } else if (value is List<*> && value.isNullOrEmpty()) {
//                    isValid = false
//                    return@forEach
//                } else if (value == null) {
//                    isValid = false
//                    return@forEach
//                }
//            }
//        }
        isFormValid.onNext(isValid)
    }

    fun getUsableStringFromMap(field: Field): String {
        return if (field.isArray())
            (fieldsMap[field] as? ArrayList<*>).toStringWithoutBrackets()
        else
            fieldsMap[field]?.toString() ?: field.value.toString()
    }

    fun submit(
        orgName: String
    ): Single<Response<OnBoardingResponse>> {
        return if (orgName == ContactPackage.TYPE_NIH) {
            nihRegRepo.onBoarding(
                orgName,
                fieldsMap
            )
        } else {
            nclRegRepo.onBoarding(
                fieldsMap
            )
        }
    }
}