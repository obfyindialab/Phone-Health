package com.example.ui.network

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.network.ConnectionType
import com.example.data.network.NetworkInfo
import com.example.data.network.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class NetworkUiState(
    val networkInfo: NetworkInfo,
    val isConnected: Boolean = networkInfo.isConnected,
    val connectionType: ConnectionType = networkInfo.connectionType,
    val isWifi: Boolean = networkInfo.isWifi,
    val isCellular: Boolean = networkInfo.isCellular
)

class NetworkViewModel @JvmOverloads constructor(
    application: Application,
    private val networkRepository: NetworkRepository = NetworkRepository(application)
) : AndroidViewModel(application) {

    /**
     * Flow of real-time network status emitted by the repository using ConnectivityManager and NetworkCapabilities.
     */
    val networkStatus: Flow<NetworkInfo> = networkRepository.networkStatusFlow

    /**
     * Exposes whether device has active internet connectivity as a StateFlow to the UI.
     */
    val isConnected: StateFlow<Boolean> = networkRepository.isConnectedFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = networkRepository.getCurrentNetworkInfo().isConnected
        )

    /**
     * Exposes active connection type (Wi-Fi, Cellular, etc.) as a StateFlow to the UI.
     */
    val connectionType: StateFlow<ConnectionType> = networkRepository.connectionTypeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = networkRepository.getCurrentNetworkInfo().connectionType
        )

    /**
     * Exposes whether connected via Wi-Fi as a StateFlow to the UI.
     */
    val isWifi: StateFlow<Boolean> = connectionType
        .map { it == ConnectionType.WIFI }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = networkRepository.getCurrentNetworkInfo().isWifi
        )

    /**
     * Exposes whether connected via Cellular/Mobile Data as a StateFlow to the UI.
     */
    val isCellular: StateFlow<Boolean> = connectionType
        .map { it == ConnectionType.CELLULAR }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = networkRepository.getCurrentNetworkInfo().isCellular
        )

    /**
     * Combined UI state for the Network Telemetry screen.
     */
    val uiState: StateFlow<NetworkUiState> = networkStatus
        .map {
            NetworkUiState(
                networkInfo = it,
                isConnected = it.isConnected,
                connectionType = it.connectionType,
                isWifi = it.isWifi,
                isCellular = it.isCellular
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkUiState(networkRepository.getCurrentNetworkInfo())
        )
}
