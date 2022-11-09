package com.merative.healthpass.ui.landing

import androidx.test.filters.SmallTest
import androidx.test.runner.AndroidJUnit4
import com.merative.healthpass.ui.landing.common.BaseTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class EraseAllCredentials : BaseTest() {

    @Test
    fun hp_E2E_TC010() { // Reference name from Excel QA document
        //erase all credentials
        testUtils.eraseAllCredentials()
    }
}