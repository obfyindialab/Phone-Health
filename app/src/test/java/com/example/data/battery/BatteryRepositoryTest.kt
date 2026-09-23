package com.example.data.battery

import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
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
class BatteryRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `getCurrentBatteryInfo returns valid BatteryInfo object`() {
        val repository = BatteryRepository(context)
        val info = repository.getCurrentBatteryInfo()

        assertNotNull(info)
        assertNotNull(info.status)
        assertNotNull(info.pluggedSource)
        assertNotNull(info.health)
    }

    @Test
    fun `battery flow receives broadcast updates with level status and temperature`() = runTest {
        val repository = BatteryRepository(context)

        // Set up battery broadcast intent
        val batteryIntent = Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_LEVEL, 85)
            putExtra(BatteryManager.EXTRA_SCALE, 100)
            putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_CHARGING)
            putExtra(BatteryManager.EXTRA_PLUGGED, BatteryManager.BATTERY_PLUGGED_AC)
            putExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
            putExtra(BatteryManager.EXTRA_TEMPERATURE, 315) // 31.5 °C
            putExtra(BatteryManager.EXTRA_VOLTAGE, 4200)
        }

        val emittedValues = mutableListOf<BatteryInfo>()
        val job = launch {
            repository.getBatteryInfoFlow().collect {
                emittedValues.add(it)
            }
        }

        // Broadcast the battery changed intent
        context.sendStickyBroadcast(batteryIntent)

        // Allow coroutine to receive
        testScheduler.advanceUntilIdle()

        assertTrue(emittedValues.isNotEmpty())
        val latest = emittedValues.last()

        assertEquals(85, latest.level)
        assertEquals(85, latest.percentage)
        assertEquals(BatteryStatus.CHARGING, latest.status)
        assertEquals(PluggedSource.AC, latest.pluggedSource)
        assertEquals(BatteryHealthStatus.GOOD, latest.health)
        assertEquals(31.5f, latest.temperatureCelsius ?: 0f, 0.1f)
        assertEquals(4200, latest.voltageMilliVolts)

        job.cancel()
    }
}
