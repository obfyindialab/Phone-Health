package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

data class DashboardCardVisibility(
    val showBattery: Boolean = true,
    val showStorage: Boolean = true,
    val showMemory: Boolean = true,
    val showCpu: Boolean = true,
    val showDevice: Boolean = true,
    val showNetwork: Boolean = true
)

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("phone_health_preferences", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _tempUnit = MutableStateFlow(loadTempUnit())
    val tempUnit: StateFlow<TemperatureUnit> = _tempUnit.asStateFlow()

    private val _cardVisibility = MutableStateFlow(loadCardVisibility())
    val cardVisibility: StateFlow<DashboardCardVisibility> = _cardVisibility.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setTempUnit(unit: TemperatureUnit) {
        prefs.edit().putString(KEY_TEMP_UNIT, unit.name).apply()
        _tempUnit.value = unit
    }

    fun updateCardVisibility(visibility: DashboardCardVisibility) {
        prefs.edit()
            .putBoolean(KEY_SHOW_BATTERY, visibility.showBattery)
            .putBoolean(KEY_SHOW_STORAGE, visibility.showStorage)
            .putBoolean(KEY_SHOW_MEMORY, visibility.showMemory)
            .putBoolean(KEY_SHOW_CPU, visibility.showCpu)
            .putBoolean(KEY_SHOW_DEVICE, visibility.showDevice)
            .putBoolean(KEY_SHOW_NETWORK, visibility.showNetwork)
            .apply()
        _cardVisibility.value = visibility
    }

    private fun loadThemeMode(): ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(name ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    }

    private fun loadTempUnit(): TemperatureUnit {
        val name = prefs.getString(KEY_TEMP_UNIT, TemperatureUnit.CELSIUS.name)
        return try {
            TemperatureUnit.valueOf(name ?: TemperatureUnit.CELSIUS.name)
        } catch (_: Exception) {
            TemperatureUnit.CELSIUS
        }
    }

    private fun loadCardVisibility(): DashboardCardVisibility {
        return DashboardCardVisibility(
            showBattery = prefs.getBoolean(KEY_SHOW_BATTERY, true),
            showStorage = prefs.getBoolean(KEY_SHOW_STORAGE, true),
            showMemory = prefs.getBoolean(KEY_SHOW_MEMORY, true),
            showCpu = prefs.getBoolean(KEY_SHOW_CPU, true),
            showDevice = prefs.getBoolean(KEY_SHOW_DEVICE, true),
            showNetwork = prefs.getBoolean(KEY_SHOW_NETWORK, true)
        )
    }

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_TEMP_UNIT = "pref_temp_unit"
        private const val KEY_SHOW_BATTERY = "pref_show_battery"
        private const val KEY_SHOW_STORAGE = "pref_show_storage"
        private const val KEY_SHOW_MEMORY = "pref_show_memory"
        private const val KEY_SHOW_CPU = "pref_show_cpu"
        private const val KEY_SHOW_DEVICE = "pref_show_device"
        private const val KEY_SHOW_NETWORK = "pref_show_network"
    }
}
