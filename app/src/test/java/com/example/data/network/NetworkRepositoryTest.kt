package com.example.data.network

import android.content.Context
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNetworkCapabilities

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NetworkRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `getCurrentNetworkInfo returns valid non-null NetworkInfo object`() {
        val repository = NetworkRepository(context)
        val info = repository.getCurrentNetworkInfo()

        assertNotNull(info)
        assertNotNull(info.connectionType)
    }

    @Test
    fun `parseCapabilities detects Wi-Fi and validated internet connection`() {
        val repository = DefaultNetworkRepository(context)
        val capabilities = ShadowNetworkCapabilities.newInstance()
        val shadow = shadowOf(capabilities)

        shadow.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        shadow.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        shadow.addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        shadow.addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        shadow.setLinkDownstreamBandwidthKbps(54000)
        shadow.setLinkUpstreamBandwidthKbps(20000)

        val info = repository.parseCapabilities(capabilities)

        assertEquals(ConnectionType.WIFI, info.connectionType)
        assertTrue(info.isWifi)
        assertFalse(info.isCellular)
        assertTrue(info.isConnected)
        assertTrue(info.isInternetValidated)
        assertFalse(info.isMetered)
        assertEquals(54000, info.downstreamBandwidthKbps)
        assertEquals(20000, info.upstreamBandwidthKbps)
        assertEquals("54.0 Mbps", info.downstreamSpeedFormatted)
    }

    @Test
    fun `parseCapabilities detects Cellular mobile data connection`() {
        val repository = DefaultNetworkRepository(context)
        val capabilities = ShadowNetworkCapabilities.newInstance()
        val shadow = shadowOf(capabilities)

        shadow.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        shadow.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        val info = repository.parseCapabilities(capabilities)

        assertEquals(ConnectionType.CELLULAR, info.connectionType)
        assertTrue(info.isCellular)
        assertFalse(info.isWifi)
        assertTrue(info.isConnected)
        assertTrue(info.isMetered) // without NOT_METERED capability
    }

    @Test
    fun `parseCapabilities handles null capabilities by returning offline state`() {
        val repository = DefaultNetworkRepository(context)
        val info = repository.parseCapabilities(null)

        assertEquals(ConnectionType.NONE, info.connectionType)
        assertFalse(info.isConnected)
        assertFalse(info.isWifi)
        assertFalse(info.isCellular)
    }

    @Test
    fun `networkStatusFlow emits current network status`() = runTest {
        val repository = NetworkRepository(context)
        val info = repository.networkStatusFlow.first()

        assertNotNull(info)
        assertNotNull(info.connectionType)
    }
}
