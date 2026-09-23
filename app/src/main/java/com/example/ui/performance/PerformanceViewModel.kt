package com.example.ui.performance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.cpu.CpuInfo
import com.example.data.cpu.CpuProvider
import com.example.data.memory.MemoryInfo
import com.example.data.memory.MemoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PerformanceUiState(
    val memoryInfo: MemoryInfo,
    val cpuInfo: CpuInfo,
    val isRefreshing: Boolean = false
)

class PerformanceViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val memoryProvider = MemoryProvider(context)
    private val cpuProvider = CpuProvider()

    private val _uiState = MutableStateFlow(
        PerformanceUiState(
            memoryInfo = memoryProvider.getMemoryInfo(),
            cpuInfo = cpuProvider.getCpuInfo()
        )
    )
    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val mem = memoryProvider.getMemoryInfo()
            val cpu = cpuProvider.getCpuInfo()
            _uiState.value = PerformanceUiState(
                memoryInfo = mem,
                cpuInfo = cpu,
                isRefreshing = false
            )
        }
    }
}
