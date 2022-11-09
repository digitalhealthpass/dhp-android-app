package com.merative.healthpass.ui.landing.common

import android.app.Activity
import android.app.Instrumentation
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.times
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.runner.AndroidJUnit4
import com.merative.healthpass.R
import com.merative.healthpass.utils.DeviceUtils
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class TestUtils {

    fun agreeTerms() {
        //click on "Agree"
        onView(withId(R.id.bottom_accept)).perform(click())
        //click on "Done"
        onView(withText("Done")).waitUntilVisible(isLoading = true, createCredentials = false)
    }
    fun completeTutorial() {
        clickNext()
        clickNext()
        onView(withText("Get Started")).perform(click())
    }

    fun enterPin() {
        onView(withHint("Enter 4 digits")).perform(click(), typeText("1234"))
    }

    fun eraseAllCredentials() {
        //Erase All credentials
        waitUntilPinScreenIsVisible()
        enterPin()
        clickProfileIcon()
        //click on "Resets"
        onView(withId(R.id.button_reset)).perform(click())
        clickCancel()
        //click on "Reset"
        onView(withId(R.id.button_reset)).perform(click())
        clickYesDelete()
    }

    fun deleteCredential() {
        //Delete Credentials
        waitUntilPinScreenIsVisible()
        enterPin()
        clickQRCodeInView(2)
        //Swipe Up on the screen to reach the bottom
        onView(withId(R.id.credential_details_header))
                .perform(swipeUp())
        //Wait till "Delete Credential" is visible
        onView(withId(R.id.cred_details_delete_textview)).waitUntilVisible(isLoading = false, createCredentials = false)
        clickCancel()
        //click on Delete Credential"
        onView(withId(R.id.cred_details_delete_textview))
                .perform(click())
        clickYesDelete()
    }

    fun duplicateCredential(imageLocation: String) {
        waitUntilPinScreenIsVisible()
        //Click on "Add to Wallet"
        enterPin()
        selectQRCode(imageLocation, allow = false, addCredentials = true, numberOfIntents =  1)
        selectQRCode(imageLocation, allow = false, addCredentials = true, numberOfIntents = 2)
        //checks to see if Warning message is displayed
        onView(withText(R.string.credential_already_added_message)).waitUntilVisible(isLoading = false, createCredentials = false)

        clickCancel()
        //re-scan the same QR code
        selectQRCode(imageLocation, allow = false, addCredentials = true, numberOfIntents = 3)
        //click "OVERWRITE" button
        onView(withText("OVERWRITE")).waitUntilVisible(isLoading = false, createCredentials = false)
    }

    private fun selectQRCode(imageLocation: String, allow: Boolean, addCredentials: Boolean, numberOfIntents: Int) {
        //create an Intent to open gallery
        val expectedIntent: Matcher<Intent> = Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_PICK),
                IntentMatchers.hasData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
        )
        //get image from gallery
        val activityResult = getGalleryIntent(imageLocation)
        Intents.intending(expectedIntent).respondWith(activityResult)
        clickAddIcon()
        onView(withText("QR code from Photo Library")).perform(click())
        if(allow) {
            //click on "Allow"
            onView(withText("Allow")).perform(click())
        }
        // Execute and Verify
        Intents.intended(expectedIntent, times(numberOfIntents))
        if(addCredentials) {
            clickAddToWallet()
        }
    }

    fun selectFromPhotoLibrary(imageLocation: String, allow: Boolean, addCredentials: Boolean, checkCredentials: Boolean,
                                viewSourceCode: Boolean, position: Int) {
        waitUntilPinScreenIsVisible()
        enterPin()
        if(allow) {
            enterPin()
            clickContinue()
        }
        selectQRCode(imageLocation, allow, addCredentials, 1)
        if(checkCredentials) {
            checkCredentialsIsDisplayed()
            onView(withText("OK")).perform(click())
        }
        if(viewSourceCode) {
            viewSourceCode(position)
        }
    }

    fun scanQRCode(checkCredentials: Boolean) {
        waitUntilPinScreenIsVisible()
        enterPin()
        if(checkCredentials) {
            enterPin()
            clickContinue()
        }
        clickAddIcon()
        //click on "Scan a QR code"
        onView(withText("Scan a QR code")).perform(click())
        if(checkCredentials) {
            checkCredentialsIsDisplayed()
        }
    }

    fun invalidScan(imageLocation: String, allow: Boolean, addCredentials: Boolean) {
        waitUntilPinScreenIsVisible()
        enterPin()
        selectQRCode(imageLocation, allow, addCredentials, 1)
        onView(withText("OK")).perform(click())
        previousScreen()
    }

    private fun viewSourceCode(position: Int) {
        clickQRCodeInView(position)
        //click on QR code Image
        onView(withId(R.id.cred_details_barcode_imageview)).perform(click())
        //checks to see if QR code Image is there
        onView(withId(R.id.fullscreen_qr_imgView)).check(matches(isDisplayed()))
        previousScreen()
        //checks to see if Details and Subject section is there
        onView(withId(R.id.cred_details_lab_results_cardview)).check(matches(isDisplayed()))
        //scroll to source View
        onView(withId(R.id.credential_details_header))
            .perform(swipeUp())
        //click on "View Source"
        onView(withId(R.id.view_source_button)).waitUntilVisible(isLoading = true, createCredentials = false)
        //click on the back button to go to previous screen
        previousScreen()
        previousScreen()
    }

    private fun getGalleryIntent(imageLocation: String): Instrumentation.ActivityResult {
        //grant permission for intent
        val intent = Intent(Intent.ACTION_PICK)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        //URI file where the image is on the device
        intent.data = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                "com.merative.watson.healthpass.wallet.qa1" + "/" + "drawable" + "/" + imageLocation)

        val resultData = Intent()
        resultData.data = intent.data

        return Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
    }

    private fun clickAddToWallet() {
        //Click on "Add to Wallet"
        onView(withId(R.id.add_to_wallet)).waitUntilVisible(isLoading = true, createCredentials = false)
    }

    fun clickProfileIcon() {
        //click on profile icon in top left
        onView((withContentDescription("Navigate up"))).waitUntilVisible(isLoading = true, createCredentials = false)
    }

    private fun clickAddIcon() {
        //click on add icon in top right corner
        onView(withId(R.id.add_credential)).waitUntilVisible(isLoading = true, createCredentials = false)
    }

    fun previousScreen() {
        //click on the back arrow button to go to previous screen
        onView(isRoot()).perform(pressBack())
    }

    fun clickCancel() {
        //click on "Cancel"
        onView(withText("Cancel")).perform(click())
    }

    private fun clickYesDelete() {
        //click "Yes, Delete"
        onView(withText("Yes, Delete")).perform(click())
    }

    private fun clickNext() {
        //click "Next"
        onView(withText("Next")).waitUntilVisible(isLoading = false, createCredentials = false)
    }

    private fun clickContinue() {
        //click "Continue"
        onView(withText("Continue")).perform(click())
    }

    private fun clickQRCodeInView(position: Int) {
        //click on first position in the recyclerView
        onView(withText("Wallet")).waitUntilVisible(isLoading = false, createCredentials = false)
        onView(withId(R.id.recyclerView))
                .perform(
                        RecyclerViewActions
                                .actionOnItemAtPosition<RecyclerView
                                .ViewHolder>(position, click()))
    }

    private fun checkCredentialsIsDisplayed() {
        onView(withText("Credential Added")).waitUntilVisible(isLoading = false, createCredentials = false)
    }

    fun waitUntilPinScreenIsVisible() {
        onView(withId(R.id.til)).waitUntilVisible(isLoading = false, createCredentials = false)
    }
    
    fun isEmulator(): Boolean {
        return DeviceUtils.isEmulator()
    }
}