package com.merative.healthpass.extensions

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.DialogFragmentNavigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController


fun Fragment.navigateSafe(@IdRes resId: Int, bundle: Bundle? = null) {
    findNavController().let {
        when (it.currentDestination) {
            is FragmentNavigator.Destination -> {
                if ((it.currentDestination as FragmentNavigator.Destination).className == this.javaClass.name) {
                    it.navigate(resId, bundle)
                }
            }
            is DialogFragmentNavigator.Destination -> {
                if ((it.currentDestination as DialogFragmentNavigator.Destination).className == this.javaClass.name) {
                    it.navigate(resId, bundle)
                }
            }
        }
    }
}