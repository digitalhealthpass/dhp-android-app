package com.merative.healthpass.ui.landing

import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.merative.healthpass.ui.landing.common.BaseTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class DuplicateCredential: BaseTest() {

    @Test
    fun hp_E2E_TC011() { // Reference name from Excel QA document
        //duplicate a credential to get error message
        val qrCodeResult = "daily_hp_green"
        testUtils.duplicateCredential(qrCodeResult)
    }
}