package com.example.domain

import com.example.data.battery.BatteryHealthStatus
import com.example.data.battery.BatteryInfo
import com.example.data.memory.MemoryInfo
import com.example.data.storage.StorageInfo
import com.example.data.thermal.ThermalThrottleStatus
import com.example.domain.model.DeviceHealthSummary
import com.example.domain.model.HardwareTelemetryStatus
import com.example.domain.model.SystemConditionLevel

class HealthScoreCalculator {

    fun calculateHealthSummary(
        batteryInfo: BatteryInfo,
        storageInfo: StorageInfo,
        memoryInfo: MemoryInfo,
        thermalThrottleStatus: ThermalThrottleStatus = ThermalThrottleStatus.UNAVAILABLE
    ): DeviceHealthSummary {
        val totalStorage = storageInfo.internalStorage.totalBytes
        val totalMemory = memoryInfo.totalBytes

        // If core hardware interfaces failed to provide any metrics
        if (totalStorage <= 0L && totalMemory <= 0L && batteryInfo.percentage == null) {
            val emptyStatus = HardwareTelemetryStatus(
                title = "Hardware Telemetry",
                statusText = "Not available on this device",
                detail = "Telemetry could not be queried",
                isNormal = true,
                isAvailable = false
            )
            return DeviceHealthSummary(
                conditionLevel = SystemConditionLevel.UNAVAILABLE,
                conditionLabel = "Telemetry Unavailable",
                conditionDescription = "System telemetry not exposed by device kernel",
                batteryStatus = emptyStatus,
                thermalStatus = emptyStatus,
                memoryStatus = emptyStatus,
                storageStatus = emptyStatus,
                isAvailable = false
            )
        }

        // 1. Battery Hardware Condition (from BatteryManager.EXTRA_HEALTH)
        val batteryHealth = batteryInfo.health
        val batteryStatusText: String
        val batteryDetail: String
        val batteryNormal: Boolean
        val batteryAvailable: Boolean

        when (batteryHealth) {
            BatteryHealthStatus.GOOD -> {
                batteryStatusText = "Good"
                batteryDetail = "Reported by OS battery driver"
                batteryNormal = true
                batteryAvailable = true
            }
            BatteryHealthStatus.UNKNOWN -> {
                batteryStatusText = "Not available on this device"
                batteryDetail = "OS did not expose health enum"
                batteryNormal = true
                batteryAvailable = false
            }
            BatteryHealthStatus.OVERHEAT -> {
                batteryStatusText = "Overheat Alert"
                batteryDetail = "High temperature flagged by OS"
                batteryNormal = false
                batteryAvailable = true
            }
            BatteryHealthStatus.DEAD -> {
                batteryStatusText = "Dead Battery"
                batteryDetail = "Battery requires service"
                batteryNormal = false
                batteryAvailable = true
            }
            BatteryHealthStatus.OVER_VOLTAGE -> {
                batteryStatusText = "Over Voltage"
                batteryDetail = "Charging voltage exceeded threshold"
                batteryNormal = false
                batteryAvailable = true
            }
            BatteryHealthStatus.UNSPECIFIED_FAILURE -> {
                batteryStatusText = "Failure Reported"
                batteryDetail = "Kernel reported power failure"
                batteryNormal = false
                batteryAvailable = true
            }
            BatteryHealthStatus.COLD -> {
                batteryStatusText = "Cold Temp Alert"
                batteryDetail = "Sub-optimal cold temperature"
                batteryNormal = false
                batteryAvailable = true
            }
        }

        val batteryTelemetry = HardwareTelemetryStatus(
            title = "Battery Condition",
            statusText = batteryStatusText,
            detail = batteryDetail,
            isNormal = batteryNormal,
            isAvailable = batteryAvailable
        )

        // 2. Thermal Status (from PowerManager.currentThermalStatus on API 29+ or battery temp)
        val thermalStatusText: String
        val thermalDetail: String
        val thermalNormal: Boolean
        val thermalAvailable: Boolean

        if (thermalThrottleStatus.isAvailable && thermalThrottleStatus != ThermalThrottleStatus.UNAVAILABLE) {
            thermalStatusText = thermalThrottleStatus.displayName
            thermalDetail = "PowerManager thermal status"
            thermalNormal = !thermalThrottleStatus.isThrottled
            thermalAvailable = true
        } else if (batteryInfo.temperatureCelsius != null) {
            val temp = batteryInfo.temperatureCelsius
            thermalStatusText = String.format("%.1f°C", temp)
            thermalNormal = temp < 46.0f
            thermalDetail = if (thermalNormal) "Battery temperature normal" else "Elevated battery temperature"
            thermalAvailable = true
        } else {
            thermalStatusText = "Not available on this device"
            thermalDetail = "Thermal sensor not exposed by kernel"
            thermalNormal = true
            thermalAvailable = false
        }

        val thermalTelemetry = HardwareTelemetryStatus(
            title = "Thermal State",
            statusText = thermalStatusText,
            detail = thermalDetail,
            isNormal = thermalNormal,
            isAvailable = thermalAvailable
        )

        // 3. Memory Headroom & Kernel Low Memory State (from ActivityManager.MemoryInfo)
        val isLowMem = memoryInfo.isLowMemory
        val availRamGb = memoryInfo.availableGb
        val memoryNormal = !isLowMem
        val memoryStatusText = if (isLowMem) "Low Memory Alert" else "Normal"
        val memoryDetail = if (isLowMem) {
            "Kernel reported memory threshold reached"
        } else {
            "${String.format("%.1f", availRamGb)} GB available RAM"
        }

        val memoryTelemetry = HardwareTelemetryStatus(
            title = "Kernel Memory",
            statusText = memoryStatusText,
            detail = memoryDetail,
            isNormal = memoryNormal,
            isAvailable = memoryInfo.totalBytes > 0L
        )

        // 4. Storage Space (from StatFs on internal partition)
        val internal = storageInfo.internalStorage
        val freeStorageGb = internal.freeGb
        val storagePct = internal.usagePercentage
        val storageNormal = storagePct < 95 && freeStorageGb >= 1.0
        val storageStatusText = if (internal.totalBytes > 0L) {
            "${String.format("%.1f", freeStorageGb)} GB Free"
        } else {
            "Not available on this device"
        }
        val storageDetail = if (internal.totalBytes > 0L) {
            if (storageNormal) "${100 - storagePct}% storage headroom" else "Critically low free space"
        } else {
            "Storage partition not accessible"
        }

        val storageTelemetry = HardwareTelemetryStatus(
            title = "Storage Headroom",
            statusText = storageStatusText,
            detail = storageDetail,
            isNormal = storageNormal,
            isAvailable = internal.totalBytes > 0L
        )

        // Overall Condition
        val hasAlert = !batteryNormal || !thermalNormal || !memoryNormal || !storageNormal
        val conditionLevel = if (hasAlert) SystemConditionLevel.ATTENTION else SystemConditionLevel.OPERATIONAL
        val conditionLabel = if (hasAlert) "Attention Needed" else "All Systems Operational"
        val conditionDesc = if (hasAlert) {
            "One or more hardware subsystems reported an active warning"
        } else {
            "Real-time Android OS telemetry verified"
        }

        return DeviceHealthSummary(
            conditionLevel = conditionLevel,
            conditionLabel = conditionLabel,
            conditionDescription = conditionDesc,
            batteryStatus = batteryTelemetry,
            thermalStatus = thermalTelemetry,
            memoryStatus = memoryTelemetry,
            storageStatus = storageTelemetry,
            isAvailable = true
        )
    }
}
