package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.battery.BatteryInfo
import com.example.data.battery.BatteryRepository
import com.example.data.cpu.CpuInfo
import com.example.data.cpu.CpuProvider
import com.example.data.device.DeviceInfo
import com.example.data.device.DeviceProvider
import com.example.data.memory.MemoryInfo
import com.example.data.memory.MemoryProvider
import com.example.data.network.NetworkInfo
import com.example.data.network.NetworkRepository
import com.example.data.settings.AppPreferences
import com.example.data.settings.DashboardCardVisibility
import com.example.data.settings.TemperatureUnit
import com.example.data.storage.StorageInfo
import com.example.data.storage.StorageProvider
import com.example.data.thermal.ThermalProvider
import com.example.domain.HealthScoreCalculator
import com.example.domain.model.DeviceHealthSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val batteryInfo: BatteryInfo,
    val storageInfo: StorageInfo,
    val memoryInfo: MemoryInfo,
    val cpuInfo: CpuInfo,
    val deviceInfo: DeviceInfo,
    val networkInfo: NetworkInfo,
    val healthSummary: DeviceHealthSummary,
    val cardVisibility: DashboardCardVisibility,
    val tempUnit: TemperatureUnit,
    val isRefreshing: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val batteryRepository = BatteryRepository(context)
    private val storageProvider = StorageProvider(context)
    private val memoryProvider = MemoryProvider(context)
    private val cpuProvider = CpuProvider()
    private val deviceProvider = DeviceProvider()
    private val networkRepository = NetworkRepository(context)
    private val appPreferences = AppPreferences(context)
    private val healthCalculator = HealthScoreCalculator()
    private val thermalProvider = ThermalProvider(context)

    private val _isRefreshing = MutableStateFlow(false)
    private val _storageInfo = MutableStateFlow(storageProvider.getStorageInfo())
    private val _memoryInfo = MutableStateFlow(memoryProvider.getMemoryInfo())
    private val _cpuInfo = MutableStateFlow(cpuProvider.getCpuInfo())
    private val _deviceInfo = MutableStateFlow(deviceProvider.getDeviceInfo())

    val uiState: StateFlow<HomeUiState> = combine(
        batteryRepository.getBatteryInfoFlow(),
        networkRepository.networkStatusFlow,
        appPreferences.cardVisibility,
        appPreferences.tempUnit,
        _isRefreshing
    ) { battery, network, visibility, tempUnit, refreshing ->
        val storage = _storageInfo.value
        val memory = _memoryInfo.value
        val cpu = _cpuInfo.value
        val device = _deviceInfo.value

        val summary = healthCalculator.calculateHealthSummary(
            batteryInfo = battery,
            storageInfo = storage,
            memoryInfo = memory,
            thermalThrottleStatus = thermalProvider.getThermalStatus()
        )

        HomeUiState(
            batteryInfo = battery,
            storageInfo = storage,
            memoryInfo = memory,
            cpuInfo = cpu,
            deviceInfo = device,
            networkInfo = network,
            healthSummary = summary,
            cardVisibility = visibility,
            tempUnit = tempUnit,
            isRefreshing = refreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(
            batteryInfo = batteryRepository.getCurrentBatteryInfo(),
            storageInfo = _storageInfo.value,
            memoryInfo = _memoryInfo.value,
            cpuInfo = _cpuInfo.value,
            deviceInfo = _deviceInfo.value,
            networkInfo = networkRepository.getCurrentNetworkInfo(),
            healthSummary = healthCalculator.calculateHealthSummary(
                batteryInfo = batteryRepository.getCurrentBatteryInfo(),
                storageInfo = _storageInfo.value,
                memoryInfo = _memoryInfo.value,
                thermalThrottleStatus = thermalProvider.getThermalStatus()
            ),
            cardVisibility = DashboardCardVisibility(),
            tempUnit = TemperatureUnit.CELSIUS,
            isRefreshing = false
        )
    )

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _storageInfo.value = storageProvider.getStorageInfo()
            _memoryInfo.value = memoryProvider.getMemoryInfo()
            _cpuInfo.value = cpuProvider.getCpuInfo()
            _deviceInfo.value = deviceProvider.getDeviceInfo()
            _isRefreshing.value = false
        }
    }
}
