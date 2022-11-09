package com.merative.healthpass.ui.tutorial

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class TutorialData(
    @DrawableRes val imageResourceId: Int,
    @StringRes val imageDescriptionStringId: Int,
    @StringRes val titleStringId: Int,
    @StringRes val subTitleStringId: Int
)