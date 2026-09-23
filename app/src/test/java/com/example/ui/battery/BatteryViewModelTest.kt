package com.example.ui.battery

import android.app.Application
import android.content.Intent
import android.os.BatteryManager
import androidx.test.core.app.ApplicationProvider
import com.example.data.battery.BatteryStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BatteryViewModelTest {

    private val application: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun `viewModel can be instantiated by AndroidViewModelFactory reflection`() {
        val constructor = BatteryViewModel::class.java.getConstructor(Application::class.java)
        assertNotNull(constructor)
        val viewModel = constructor.newInstance(application)
        assertNotNull(viewModel)
    }

    @Test
    fun `viewModel exposes initial battery state and updates on stream`() = runTest {
        // Sticky broadcast
        val intent = Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_LEVEL, 72)
            putExtra(BatteryManager.EXTRA_SCALE, 100)
            putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_DISCHARGING)
            putExtra(BatteryManager.EXTRA_TEMPERATURE, 285) // 28.5 °C
        }
        application.sendStickyBroadcast(intent)

        val viewModel = BatteryViewModel(application)
        val state = viewModel.uiState.first { it.batteryInfo.percentage == 72 }

        assertNotNull(state)
        assertEquals(72, state.batteryInfo.level)
        assertEquals(BatteryStatus.DISCHARGING, state.batteryInfo.status)
        assertEquals(28.5f, state.batteryInfo.temperatureCelsius ?: 0f, 0.1f)
    }

    @Test
    fun `viewModel exposes batteryLevel chargingStatus and temperature flows to UI`() = runTest {
        val intent = Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_LEVEL, 95)
            putExtra(BatteryManager.EXTRA_SCALE, 100)
            putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_CHARGING)
            putExtra(BatteryManager.EXTRA_PLUGGED, BatteryManager.BATTERY_PLUGGED_AC)
            putExtra(BatteryManager.EXTRA_TEMPERATURE, 325) // 32.5 °C
        }
        application.sendStickyBroadcast(intent)

        val viewModel = BatteryViewModel(application)

        val level = viewModel.batteryLevel.first { it == 95 }
        val status = viewModel.chargingStatus.first { it == BatteryStatus.CHARGING }
        val isCharging = viewModel.isCharging.first { it }
        val temp = viewModel.temperature.first { it != null && it > 32f }
        val batteryTemp = viewModel.batteryTemperature.first { it != null && it > 32f }

        assertEquals(95, level)
        assertEquals(BatteryStatus.CHARGING, status)
        assertTrue(isCharging)
        assertEquals(32.5f, temp ?: 0f, 0.1f)
        assertEquals(32.5f, batteryTemp ?: 0f, 0.1f)
    }

    @Test
    fun `viewModel updates when discharging broadcast received`() = runTest {
        val intent = Intent(Intent.ACTION_BATTERY_CHANGED).apply {
            putExtra(BatteryManager.EXTRA_LEVEL, 45)
            putExtra(BatteryManager.EXTRA_SCALE, 100)
            putExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_DISCHARGING)
            putExtra(BatteryManager.EXTRA_PLUGGED, 0)
            putExtra(BatteryManager.EXTRA_TEMPERATURE, 290) // 29.0 °C
        }
        application.sendStickyBroadcast(intent)

        val viewModel = BatteryViewModel(application)

        val level = viewModel.batteryLevel.first { it == 45 }
        val status = viewModel.chargingStatus.first { it == BatteryStatus.DISCHARGING }
        val isCharging = viewModel.isCharging.first { !it }

        assertEquals(45, level)
        assertEquals(BatteryStatus.DISCHARGING, status)
        assertFalse(isCharging)
    }
}
