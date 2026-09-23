package com.example.data.hardware

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HardwareRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `getBuildDetails returns valid build parameters using Build constants`() {
        val repository = HardwareRepository(context)
        val details = repository.getBuildDetails()

        assertNotNull(details)
        assertEquals(Build.MANUFACTURER.replaceFirstChar { it.uppercase() }, details.manufacturer)
        assertEquals(Build.MODEL, details.model)
        assertEquals(Build.VERSION.RELEASE, details.androidVersion)
        assertEquals(Build.VERSION.SDK_INT, details.sdkInt)
        assertTrue(details.versionFormatted.contains("Android"))
        assertTrue(details.deviceFormatted.isNotBlank())
    }

    @Test
    fun `getBuildDetailsFlow emits build details successfully`() = runTest {
        val repository = HardwareRepository(context)
        val details = repository.getBuildDetailsFlow().first()

        assertNotNull(details)
        assertEquals(Build.MANUFACTURER.replaceFirstChar { it.uppercase() }, details.manufacturer)
        assertEquals(Build.MODEL, details.model)
        assertEquals(Build.VERSION.RELEASE, details.androidVersion)
    }

    @Test
    fun `getDeviceInfo matches build details`() {
        val repository = HardwareRepository(context)
        val device = repository.getDeviceInfo()

        assertNotNull(device)
        assertEquals(Build.MANUFACTURER.replaceFirstChar { it.uppercase() }, device.manufacturer)
        assertEquals(Build.MODEL, device.model)
        assertEquals(Build.VERSION.RELEASE, device.androidVersion)
    }

    @Test
    fun `getHardwareInfo retrieves display and sensor structures`() {
        val repository = HardwareRepository(context)
        val hw = repository.getHardwareInfo()

        assertNotNull(hw)
        assertNotNull(hw.displayInfo)
        assertNotNull(hw.cameraSummary)
        assertNotNull(hw.sensors)
    }
}
