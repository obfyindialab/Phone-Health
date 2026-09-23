package com.example.data.thermal

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ThermalProviderTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `getThermalStatus returns valid status without throwing`() {
        val provider = ThermalProvider(context)
        val status = provider.getThermalStatus()

        assertNotNull(status)
        assertNotNull(status.displayName)
    }
}
