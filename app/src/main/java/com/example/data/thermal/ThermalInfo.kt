package com.example.data.thermal

enum class ThermalThrottleStatus(
    val displayName: String,
    val isThrottled: Boolean,
    val isAvailable: Boolean = true
) {
    NONE("Normal (No Throttling)", false),
    LIGHT("Light Throttling", true),
    MODERATE("Moderate Throttling", true),
    SEVERE("Severe Throttling", true),
    CRITICAL("Critical Thermal Alert", true),
    EMERGENCY("Emergency Thermal State", true),
    SHUTDOWN("Thermal Shutdown Imminent", true),
    UNAVAILABLE("Not available on this device", false, isAvailable = false)
}

data class ThermalInfo(
    val status: ThermalThrottleStatus,
    val temperatureCelsius: Float? = null
)
