package com.merative.healthpass.models.credential

import com.merative.healthpass.models.sharedPref.Package

data class SelectCredentials(
    val availableList: ArrayList<Package>,
    val unavailableList: ArrayList<Package>,
    val unsupportedList: ArrayList<Package>,
)
