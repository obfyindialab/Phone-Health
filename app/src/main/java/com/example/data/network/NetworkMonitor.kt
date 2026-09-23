package com.example.data.network

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Legacy monitor that delegates to [NetworkRepository].
 */
@Deprecated(
    message = "Use NetworkRepository directly for network telemetry operations",
    replaceWith = ReplaceWith("NetworkRepository(context)", "com.example.data.network.NetworkRepository")
)
class NetworkMonitor(context: Context) {

    private val repository: NetworkRepository = NetworkRepository(context)

    fun getNetworkInfoFlow(): Flow<NetworkInfo> = repository.getNetworkInfoFlow()

    fun readCurrentNetworkInfo(): NetworkInfo = repository.getCurrentNetworkInfo()
}
