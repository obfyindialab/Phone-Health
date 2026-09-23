package com.example.ui.settings

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.settings.AppPreferences
import com.example.data.settings.DashboardCardVisibility
import com.example.data.settings.TemperatureUnit
import com.example.data.settings.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PermissionStatus(
    val permission: String,
    val name: String,
    val description: String,
    val isGranted: Boolean,
    val isRequired: Boolean
)

data class SettingsUiState(
    val themeMode: ThemeMode,
    val tempUnit: TemperatureUnit,
    val cardVisibility: DashboardCardVisibility,
    val permissions: List<PermissionStatus>,
    val appVersion: String = "1.0.0"
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val appPreferences = AppPreferences(context)

    val uiState: StateFlow<SettingsUiState> = combine(
        appPreferences.themeMode,
        appPreferences.tempUnit,
        appPreferences.cardVisibility
    ) { mode, unit, visibility ->
        val networkGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED

        val permList = listOf(
            PermissionStatus(
                permission = Manifest.permission.ACCESS_NETWORK_STATE,
                name = "Network State Access",
                description = "Used solely to detect whether Wi-Fi or cellular network is active on device.",
                isGranted = networkGranted,
                isRequired = true
            )
        )

        SettingsUiState(
            themeMode = mode,
            tempUnit = unit,
            cardVisibility = visibility,
            permissions = permList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(
            themeMode = ThemeMode.SYSTEM,
            tempUnit = TemperatureUnit.CELSIUS,
            cardVisibility = DashboardCardVisibility(),
            permissions = emptyList()
        )
    )

    fun setThemeMode(mode: ThemeMode) {
        appPreferences.setThemeMode(mode)
    }

    fun setTempUnit(unit: TemperatureUnit) {
        appPreferences.setTempUnit(unit)
    }

    fun toggleCard(key: String, enabled: Boolean) {
        val current = uiState.value.cardVisibility
        val updated = when (key) {
            "battery" -> current.copy(showBattery = enabled)
            "storage" -> current.copy(showStorage = enabled)
            "memory" -> current.copy(showMemory = enabled)
            "cpu" -> current.copy(showCpu = enabled)
            "device" -> current.copy(showDevice = enabled)
            "network" -> current.copy(showNetwork = enabled)
            else -> current
        }
        appPreferences.updateCardVisibility(updated)
    }
}
