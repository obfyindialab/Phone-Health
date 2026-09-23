package com.example.domain

import com.example.data.battery.BatteryHealthStatus
import com.example.data.battery.BatteryInfo
import com.example.data.battery.BatteryStatus
import com.example.data.battery.PluggedSource
import com.example.data.memory.MemoryInfo
import com.example.data.storage.StorageInfo
import com.example.data.storage.StoragePartition
import com.example.data.thermal.ThermalThrottleStatus
import com.example.domain.model.SystemConditionLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthScoreCalculatorTest {

    private val calculator = HealthScoreCalculator()

    private fun sampleBattery(
        health: BatteryHealthStatus = BatteryHealthStatus.GOOD,
        percentage: Int? = 80,
        tempCelsius: Float? = 32.0f
    ) = BatteryInfo(
        percentage = percentage,
        status = BatteryStatus.DISCHARGING,
        pluggedSource = PluggedSource.BATTERY,
        health = health,
        temperatureCelsius = tempCelsius,
        voltageMilliVolts = 4000
    )

    private fun sampleMemory(isLowMemory: Boolean = false): MemoryInfo {
        val total = 8_000_000_000L
        val avail = 4_000_000_000L
        val used = total - avail
        val pct = ((used.toDouble() / total.toDouble()) * 100).toInt()
        return MemoryInfo(
            totalBytes = total,
            availableBytes = avail,
            usedBytes = used,
            usagePercentage = pct,
            isLowMemory = isLowMemory,
            thresholdBytes = 1_000_000_000L
        )
    }

    private fun sampleStorage(usagePercentage: Int = 50) = StorageInfo(
        internalStorage = StoragePartition(
            name = "Internal Storage",
            path = "/data",
            totalBytes = 128_000_000_000L,
            availableBytes = 64_000_000_000L,
            freeBytes = 64_000_000_000L,
            usedBytes = 64_000_000_000L,
            usagePercentage = usagePercentage,
            isRemovable = false
        ),
        externalStorage = null
    )

    @Test
    fun `healthy device returns OPERATIONAL with all systems operational`() {
        val summary = calculator.calculateHealthSummary(
            batteryInfo = sampleBattery(),
            storageInfo = sampleStorage(),
            memoryInfo = sampleMemory(),
            thermalThrottleStatus = ThermalThrottleStatus.NONE
        )

        assertEquals(SystemConditionLevel.OPERATIONAL, summary.conditionLevel)
        assertEquals("All Systems Operational", summary.conditionLabel)
        assertEquals("Good", summary.batteryStatus.statusText)
        assertTrue(summary.batteryStatus.isNormal)
        assertTrue(summary.memoryStatus.isNormal)
        assertTrue(summary.storageStatus.isNormal)
        assertTrue(summary.thermalStatus.isNormal)
    }

    @Test
    fun `kernel low memory alert triggers ATTENTION condition`() {
        val summary = calculator.calculateHealthSummary(
            batteryInfo = sampleBattery(),
            storageInfo = sampleStorage(),
            memoryInfo = sampleMemory(isLowMemory = true),
            thermalThrottleStatus = ThermalThrottleStatus.NONE
        )

        assertEquals(SystemConditionLevel.ATTENTION, summary.conditionLevel)
        assertEquals("Attention Needed", summary.conditionLabel)
        assertEquals("Low Memory Alert", summary.memoryStatus.statusText)
        assertFalse(summary.memoryStatus.isNormal)
    }

    @Test
    fun `battery overheat alert triggers ATTENTION condition`() {
        val summary = calculator.calculateHealthSummary(
            batteryInfo = sampleBattery(health = BatteryHealthStatus.OVERHEAT),
            storageInfo = sampleStorage(),
            memoryInfo = sampleMemory(),
            thermalThrottleStatus = ThermalThrottleStatus.NONE
        )

        assertEquals(SystemConditionLevel.ATTENTION, summary.conditionLevel)
        assertEquals("Overheat Alert", summary.batteryStatus.statusText)
        assertFalse(summary.batteryStatus.isNormal)
    }

    @Test
    fun `unknown battery health shows not available on this device`() {
        val summary = calculator.calculateHealthSummary(
            batteryInfo = sampleBattery(health = BatteryHealthStatus.UNKNOWN),
            storageInfo = sampleStorage(),
            memoryInfo = sampleMemory(),
            thermalThrottleStatus = ThermalThrottleStatus.NONE
        )

        assertEquals("Not available on this device", summary.batteryStatus.statusText)
        assertFalse(summary.batteryStatus.isAvailable)
    }

    @Test
    fun `unexposed thermal status shows not available on this device`() {
        val summary = calculator.calculateHealthSummary(
            batteryInfo = sampleBattery(tempCelsius = null),
            storageInfo = sampleStorage(),
            memoryInfo = sampleMemory(),
            thermalThrottleStatus = ThermalThrottleStatus.UNAVAILABLE
        )

        assertEquals("Not available on this device", summary.thermalStatus.statusText)
        assertFalse(summary.thermalStatus.isAvailable)
    }
}
