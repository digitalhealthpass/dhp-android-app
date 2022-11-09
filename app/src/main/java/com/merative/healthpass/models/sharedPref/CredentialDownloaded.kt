package com.merative.healthpass.models.sharedPref

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CredentialDownloaded(val id: String, val type: List<String>) : Parcelable
