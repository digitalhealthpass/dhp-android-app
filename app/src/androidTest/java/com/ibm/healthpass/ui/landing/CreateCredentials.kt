package com.merative.healthpass.ui.landing

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.merative.healthpass.R
import com.merative.healthpass.ui.landing.common.BaseTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class CreateCredentials: BaseTest() {

    @Test
    fun createCredentials() {
        /* create credentials
        * you must save the credentials manually from your chosen application
        */
        testUtils.waitUntilPinScreenIsVisible()
        testUtils.enterPin()
        testUtils.clickProfileIcon()
        clickCreateCredentials()
        testUtils.clickCancel()
        clickCreateCredentials()
        clickCreate()
        Espresso.onView(ViewMatchers.withText(R.string.password_empty_message))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        clickCreateCredentials()
        Espresso.onView(ViewMatchers.withHint(R.string.label_placeholder_password))
            .perform(ViewActions.click(), ViewActions.typeText("Wallet"))
        clickCreate()
        // Waiting until you choose where to store the credential until Settings screen is back
        Espresso.onView(ViewMatchers.withText("Settings"))
            .waitUntilVisible(isLoading = false, createCredentials = true)
        testUtils.previousScreen()
    }

    private fun clickCreateCredentials() {
        Espresso.onView(ViewMatchers.withId(R.id.create_backup_button)).perform(ViewActions.click())
    }

    private fun clickCreate() {
        Espresso.onView(ViewMatchers.withText("Create")).perform(ViewActions.click())
    }
}