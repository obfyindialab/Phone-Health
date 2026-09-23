package com.example.ui.battery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.battery.BatteryInfo
import com.example.data.battery.BatteryRepository
import com.example.data.battery.BatteryStatus
import com.example.data.settings.AppPreferences
import com.example.data.settings.TemperatureUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class BatteryUiState(
    val batteryInfo: BatteryInfo,
    val tempUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val batteryLevel: Int? = batteryInfo.level,
    val chargingStatus: BatteryStatus = batteryInfo.status,
    val temperature: Float? = batteryInfo.temperatureCelsius,
    val isCharging: Boolean = batteryInfo.status == BatteryStatus.CHARGING
)

class BatteryViewModel @JvmOverloads constructor(
    application: Application,
    private val batteryRepository: BatteryRepository = BatteryRepository(application)
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val appPreferences = AppPreferences(context)

    /**
     * Flow of real-time battery info streamed from IntentFilter(Intent.ACTION_BATTERY_CHANGED).
     */
    val batteryInfoFlow: Flow<BatteryInfo> = batteryRepository.getBatteryInfoFlow()

    /**
     * Exposes battery level (0-100%) as a Flow (StateFlow) to the UI.
     */
    val batteryLevel: StateFlow<Int?> = batteryInfoFlow
        .map { it.level }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = batteryRepository.getCurrentBatteryInfo().level
        )

    /**
     * Exposes charging status (CHARGING, DISCHARGING, FULL, NOT_CHARGING, UNKNOWN) as a Flow (StateFlow) to the UI.
     */
    val chargingStatus: StateFlow<BatteryStatus> = batteryInfoFlow
        .map { it.status }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = batteryRepository.getCurrentBatteryInfo().status
        )

    /**
     * Exposes whether the device is actively charging as a Flow (StateFlow) to the UI.
     */
    val isCharging: StateFlow<Boolean> = batteryInfoFlow
        .map { it.status == BatteryStatus.CHARGING }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = batteryRepository.getCurrentBatteryInfo().status == BatteryStatus.CHARGING
        )

    /**
     * Exposes battery temperature in Celsius as a Flow (StateFlow) to the UI.
     */
    val temperature: StateFlow<Float?> = batteryInfoFlow
        .map { it.temperatureCelsius }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = batteryRepository.getCurrentBatteryInfo().temperatureCelsius
        )

    /**
     * Explicit alias for [temperature] as a Flow (StateFlow) to the UI.
     */
    val batteryTemperature: StateFlow<Float?> = temperature

    /**
     * Full UI state combining real-time battery telemetry and user unit preferences.
     */
    val uiState: StateFlow<BatteryUiState> = combine(
        batteryInfoFlow,
        appPreferences.tempUnit
    ) { battery, tempUnit ->
        BatteryUiState(
            batteryInfo = battery,
            tempUnit = tempUnit,
            batteryLevel = battery.level,
            chargingStatus = battery.status,
            temperature = battery.temperatureCelsius,
            isCharging = battery.status == BatteryStatus.CHARGING
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BatteryUiState(
            batteryInfo = batteryRepository.getCurrentBatteryInfo(),
            tempUnit = TemperatureUnit.CELSIUS
        )
    )
}
