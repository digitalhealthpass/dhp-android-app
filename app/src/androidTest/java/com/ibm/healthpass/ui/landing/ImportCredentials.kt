package com.merative.healthpass.ui.landing

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.merative.healthpass.R
import com.merative.healthpass.ui.landing.common.BaseTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class ImportCredentials: BaseTest() {

    @Test
    fun importCredentials() {
        /* imports credentials
        * you must select the credentials manually from your chosen application
        */
        testUtils.waitUntilPinScreenIsVisible()
        testUtils.enterPin()
        testUtils.clickProfileIcon()
        clickImportCredentials()
        testUtils.clickCancel()
        clickImportCredentials()
        Espresso.onView(ViewMatchers.withHint(R.string.label_placeholder_password))
            .perform(ViewActions.click(), ViewActions.typeText("Wallet"))
        clickImport()
        // Waiting until you choose where to store the credential until Credentials is imported
        Espresso.onView(ViewMatchers.withText("OK"))
            .waitUntilVisible(isLoading = false, createCredentials = true)
    }

    private fun clickImportCredentials() {
        Espresso.onView(ViewMatchers.withId(R.id.import_backup_button)).perform(ViewActions.click())
    }


    private fun clickImport() {
        Espresso.onView(ViewMatchers.withText("Import")).perform(ViewActions.click())
    }
}