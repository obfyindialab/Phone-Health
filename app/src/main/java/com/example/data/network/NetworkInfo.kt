package com.example.data.network

enum class ConnectionType(val displayName: String) {
    WIFI("Wi-Fi"),
    CELLULAR("Mobile Data"),
    ETHERNET("Ethernet"),
    VPN("VPN"),
    BLUETOOTH("Bluetooth Tethering"),
    NONE("Offline"),
    UNKNOWN("Unknown")
}

data class NetworkInfo(
    val connectionType: ConnectionType,
    val isConnected: Boolean,
    val isInternetValidated: Boolean,
    val isMetered: Boolean,
    val downstreamBandwidthKbps: Int?,
    val upstreamBandwidthKbps: Int?
) {
    val isWifi: Boolean
        get() = connectionType == ConnectionType.WIFI

    val isCellular: Boolean
        get() = connectionType == ConnectionType.CELLULAR

    val downstreamSpeedFormatted: String?
        get() = downstreamBandwidthKbps?.takeIf { it > 0 }?.let {
            if (it >= 1000) String.format("%.1f Mbps", it / 1000f) else "$it Kbps"
        }

    val upstreamSpeedFormatted: String?
        get() = upstreamBandwidthKbps?.takeIf { it > 0 }?.let {
            if (it >= 1000) String.format("%.1f Mbps", it / 1000f) else "$it Kbps"
        }
}
