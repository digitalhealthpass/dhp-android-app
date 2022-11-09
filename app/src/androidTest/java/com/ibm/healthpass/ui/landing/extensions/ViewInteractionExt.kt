package com.merative.healthpass.ui.landing.extensions

import androidx.test.espresso.*
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*


/** Perform action of waiting for a specific view id.  */
fun ViewInteraction.waitUntilVisible(isLoading: Boolean, createCredentials: Boolean): ViewInteraction {
    val timeout = 30000 //up to 30 seconds before timing out
    val startTime = System.currentTimeMillis()
    val endTime = startTime + timeout

    do {
        try {
            //checks to see if view is displayed
            if(createCredentials) {
                SystemClock.sleep(1500)
            }
            check(matches(isDisplayed()))
            if(isLoading) {
                SystemClock.sleep(1500)
            }
            perform(click())
            return this
        } catch (e: NoMatchingViewException) {
            Thread.sleep(50)
        }
    } while (System.currentTimeMillis() < endTime)

    throw TimeoutException()
}

