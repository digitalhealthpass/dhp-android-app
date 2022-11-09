package com.merative.healthpass.ui.registration.selection

import android.content.Context
import com.merative.healthpass.extensions.loadAssetsFileAsString
import com.merative.watson.healthpass.verifiablecredential.extensions.parse
import com.merative.healthpass.models.Location
import com.merative.healthpass.ui.common.BaseViewModel
import javax.inject.Inject

class FieldSelectionVM @Inject constructor(
    private val context: Context
) : BaseViewModel() {
    val locationList = ArrayList<Location>()

    init {
        loadLocations()
    }

    private fun loadLocations() {
        locationList.addAll(
            parse(context.loadAssetsFileAsString(PATH_LOCATION))
        )
    }

    companion object {
        const val PATH_LOCATION: String = "mock/location.json"
    }
}