package com.example.ui.storage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.storage.StorageInfo
import com.example.data.storage.StorageProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StorageUiState(
    val storageInfo: StorageInfo,
    val isRefreshing: Boolean = false
)

class StorageViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val storageProvider = StorageProvider(context)

    private val _uiState = MutableStateFlow(
        StorageUiState(storageInfo = storageProvider.getStorageInfo())
    )
    val uiState: StateFlow<StorageUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val info = storageProvider.getStorageInfo()
            _uiState.value = StorageUiState(storageInfo = info, isRefreshing = false)
        }
    }
}
