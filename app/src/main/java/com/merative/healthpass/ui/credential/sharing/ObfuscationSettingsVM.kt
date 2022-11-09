package com.merative.healthpass.ui.credential.sharing

import com.google.gson.JsonElement
import com.merative.healthpass.ui.common.BaseViewModel
import javax.inject.Inject

class ObfuscationSettingsVM @Inject constructor() :
    BaseViewModel() {
    var obfuscationObjectList = HashMap<JsonElement, Boolean>()
}