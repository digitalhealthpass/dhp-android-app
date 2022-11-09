package com.merative.healthpass.models.sharedPref

import android.os.Parcelable
import com.merative.watson.healthpass.verifiablecredential.extensions.localeFormat
import kotlinx.parcelize.Parcelize
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@Parcelize
data class AutoReset(
    var frequency: Int,
    var lastAutoResetDate: Calendar,
    var isEnabled: Boolean
) : Parcelable {

    fun getFormattedLastAutoResetDate(): String {
        return lastAutoResetDate.time.localeFormat(DateFormat.SHORT)
        //lastAutoResetDate.toString()
    }

    fun isTimeToReset(): Boolean {

        val now = Calendar.getInstance()
        val timeInDays =
            TimeUnit.MILLISECONDS.toDays(abs(now.timeInMillis - lastAutoResetDate.timeInMillis))

        when (frequency) {
            TYPE_DAILY -> {
                if (timeInDays >= 1) {
                    return true
                }
            }
            TYPE_WEEKLY -> {
                if (timeInDays > 7) {
                    return true
                }
            }
            TYPE_MONTHLY -> {
                if (timeInDays > 30) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        //Frequency Types
        const val TYPE_DAILY = 0
        const val TYPE_WEEKLY = 1
        const val TYPE_MONTHLY = 2

        val EMPTY = AutoReset(TYPE_DAILY, Calendar.getInstance(), true)
    }
}