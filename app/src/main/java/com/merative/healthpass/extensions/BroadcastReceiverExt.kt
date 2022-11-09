package com.merative.healthpass.extensions

import android.content.BroadcastReceiver
import androidx.fragment.app.FragmentActivity

@Throws(
    IllegalArgumentException::class
)
fun BroadcastReceiver?.unregisterReceiver(activity: FragmentActivity?) {
    if (this != null) activity?.unregisterReceiver(this)
}