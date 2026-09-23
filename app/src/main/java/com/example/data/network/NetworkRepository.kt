package com.example.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Repository interface for detecting active network connection types (Wi-Fi, Cellular, etc.)
 * and internet connectivity using [ConnectivityManager] and [NetworkCapabilities],
 * exposing network status as a data flow.
 */
interface NetworkRepository {

    /**
     * Flow of real-time [NetworkInfo] updates whenever network availability
     * or capabilities change.
     */
    val networkStatusFlow: Flow<NetworkInfo>

    /**
     * Streams network info updates as a Flow.
     */
    fun getNetworkInfoFlow(): Flow<NetworkInfo> = networkStatusFlow

    /**
     * Streams whether the device currently has active internet connectivity.
     */
    val isConnectedFlow: Flow<Boolean>
        get() = networkStatusFlow.map { it.isConnected }.distinctUntilChanged()

    /**
     * Streams the active connection type (e.g. [ConnectionType.WIFI], [ConnectionType.CELLULAR]).
     */
    val connectionTypeFlow: Flow<ConnectionType>
        get() = networkStatusFlow.map { it.connectionType }.distinctUntilChanged()

    /**
     * Synchronously queries current active network telemetry.
     */
    fun getCurrentNetworkInfo(): NetworkInfo

    companion object {
        operator fun invoke(context: Context): NetworkRepository =
            DefaultNetworkRepository(context.applicationContext)
    }
}

/**
 * Default implementation of [NetworkRepository] monitoring network connectivity
 * and active transports via Android's [ConnectivityManager] and [NetworkCapabilities].
 */
class DefaultNetworkRepository(
    private val context: Context,
    private val connectivityManager: ConnectivityManager? =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
) : NetworkRepository {

    override val networkStatusFlow: Flow<NetworkInfo> = callbackFlow {
        // Emit current active connection state immediately
        trySend(getCurrentNetworkInfo())

        val cm = connectivityManager
        if (cm == null) {
            close()
            return@callbackFlow
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getCurrentNetworkInfo())
            }

            override fun onLost(network: Network) {
                trySend(getCurrentNetworkInfo())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(parseCapabilities(networkCapabilities))
            }
        }

        try {
            val request = NetworkRequest.Builder().build()
            cm.registerNetworkCallback(request, callback)
        } catch (_: SecurityException) {
            // Permission or sandbox restriction fallback
        } catch (_: Exception) {
            try {
                cm.registerDefaultNetworkCallback(callback)
            } catch (_: Exception) {}
        }

        awaitClose {
            try {
                cm.unregisterNetworkCallback(callback)
            } catch (_: Exception) {
                // Ignore if already unregistered
            }
        }
    }.distinctUntilChanged()

    override fun getCurrentNetworkInfo(): NetworkInfo {
        val cm = connectivityManager ?: return fallbackNetworkInfo()
        return try {
            val activeNetwork = cm.activeNetwork ?: return fallbackNetworkInfo()
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return fallbackNetworkInfo()
            parseCapabilities(capabilities)
        } catch (_: Exception) {
            fallbackNetworkInfo()
        }
    }

    fun parseCapabilities(capabilities: NetworkCapabilities?): NetworkInfo {
        if (capabilities == null) return fallbackNetworkInfo()

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isValidated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        val isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        val connectionType = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> ConnectionType.VPN
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> ConnectionType.BLUETOOTH
            else -> if (hasInternet) ConnectionType.UNKNOWN else ConnectionType.NONE
        }

        val downKbps = capabilities.linkDownstreamBandwidthKbps.takeIf { it > 0 }
        val upKbps = capabilities.linkUpstreamBandwidthKbps.takeIf { it > 0 }

        return NetworkInfo(
            connectionType = connectionType,
            isConnected = hasInternet,
            isInternetValidated = isValidated,
            isMetered = isMetered,
            downstreamBandwidthKbps = downKbps,
            upstreamBandwidthKbps = upKbps
        )
    }

    private fun fallbackNetworkInfo(): NetworkInfo {
        return NetworkInfo(
            connectionType = ConnectionType.NONE,
            isConnected = false,
            isInternetValidated = false,
            isMetered = false,
            downstreamBandwidthKbps = null,
            upstreamBandwidthKbps = null
        )
    }
}
