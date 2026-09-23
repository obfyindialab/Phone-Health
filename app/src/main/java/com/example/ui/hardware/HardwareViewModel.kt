package com.example.ui.hardware

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.device.DeviceInfo
import com.example.data.hardware.BuildDetails
import com.example.data.hardware.HardwareInfo
import com.example.data.hardware.HardwareRepository
import com.example.data.hardware.SensorCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HardwareUiState(
    val buildDetails: BuildDetails,
    val deviceInfo: DeviceInfo,
    val hardwareInfo: HardwareInfo,
    val selectedSensorCategory: SensorCategory? = null
)

class HardwareViewModel @JvmOverloads constructor(
    application: Application,
    private val hardwareRepository: HardwareRepository = HardwareRepository(application)
) : AndroidViewModel(application) {

    /**
     * Flow emitting build details (manufacturer, model, androidVersion, etc.) fetched via Build constants.
     */
    val buildDetails: StateFlow<BuildDetails> = hardwareRepository.getBuildDetailsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = hardwareRepository.getBuildDetails()
        )

    /**
     * Exposes device manufacturer as a StateFlow to the UI.
     */
    val manufacturer: StateFlow<String> = buildDetails
        .map { it.manufacturer }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = hardwareRepository.getBuildDetails().manufacturer
        )

    /**
     * Exposes device model as a StateFlow to the UI.
     */
    val model: StateFlow<String> = buildDetails
        .map { it.model }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = hardwareRepository.getBuildDetails().model
        )

    /**
     * Exposes Android version as a StateFlow to the UI.
     */
    val androidVersion: StateFlow<String> = buildDetails
        .map { it.androidVersion }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = hardwareRepository.getBuildDetails().androidVersion
        )

    private val _uiState = MutableStateFlow(
        HardwareUiState(
            buildDetails = hardwareRepository.getBuildDetails(),
            deviceInfo = hardwareRepository.getDeviceInfo(),
            hardwareInfo = hardwareRepository.getHardwareInfo(),
            selectedSensorCategory = null
        )
    )
    val uiState: StateFlow<HardwareUiState> = _uiState.asStateFlow()

    fun selectSensorCategory(category: SensorCategory?) {
        _uiState.value = _uiState.value.copy(selectedSensorCategory = category)
    }

    fun refresh() {
        val newBuild = hardwareRepository.getBuildDetails()
        val newDevice = hardwareRepository.getDeviceInfo()
        val newHw = hardwareRepository.getHardwareInfo()

        _uiState.value = _uiState.value.copy(
            buildDetails = newBuild,
            deviceInfo = newDevice,
            hardwareInfo = newHw
        )
    }
}
