package com.merative.healthpass.ui.debug.debugFragment

import com.merative.healthpass.ui.common.BaseViewModel
import com.merative.healthpass.ui.region.EnvironmentHandler
import com.merative.healthpass.utils.pref.SharedPrefUtils
import javax.inject.Inject

class DebugVM @Inject constructor(
    val environmentHandler: EnvironmentHandler,
    val sharedPrefUtils: SharedPrefUtils,
) : BaseViewModel()