package com.example.domain.model

enum class SystemConditionLevel {
    OPERATIONAL,
    ATTENTION,
    UNAVAILABLE
}

data class HardwareTelemetryStatus(
    val title: String,
    val statusText: String,
    val detail: String,
    val isNormal: Boolean,
    val isAvailable: Boolean = true
)

data class DeviceHealthSummary(
    val conditionLevel: SystemConditionLevel,
    val conditionLabel: String,
    val conditionDescription: String,
    val batteryStatus: HardwareTelemetryStatus,
    val thermalStatus: HardwareTelemetryStatus,
    val memoryStatus: HardwareTelemetryStatus,
    val storageStatus: HardwareTelemetryStatus,
    val isAvailable: Boolean = true,
    val disclaimer: String = "Informational hardware telemetry obtained directly from Android system services (BatteryManager, PowerManager, ActivityManager, StatFs). Zero estimations."
) {
    // Backward-compatible alias
    val summaryLabel: String get() = conditionLabel

    val statuses: List<HardwareTelemetryStatus>
        get() = listOf(batteryStatus, thermalStatus, memoryStatus, storageStatus)
}
