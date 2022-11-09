package com.merative.healthpass.ui.landing.common

import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import com.merative.healthpass.ui.mainActivity.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
open class BaseTest {

    @get:Rule
    var activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    protected var testUtils = TestUtils()

    @Before
    fun disableAnimations() {
        //animations needs to be turned off as automation test cases might fail
        toggleAnimation(false)
        Intents.init()
    }

    @After
    fun enableAnimations() {
        //turning animations back on after test ends
        Intents.release()
        toggleAnimation(true)
    }

    private fun toggleAnimation(enable: Boolean) {
        with(UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())) {
            executeShellCommand("settings put global transition_animation_scale ${if (enable) 1 else 0}")
            executeShellCommand("settings put global window_animation_scale ${if (enable) 1 else 0}")
            executeShellCommand("settings put global animator_duration_scale ${if (enable) 1 else 0}")
        }
    }
}