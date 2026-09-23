package com.example.ui.network

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.network.ConnectionType
import com.example.data.network.NetworkInfo
import com.example.data.network.NetworkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NetworkViewModelTest {

    private val application: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun `viewModel can be instantiated by AndroidViewModelFactory reflection`() {
        val constructor = NetworkViewModel::class.java.getConstructor(Application::class.java)
        assertNotNull(constructor)
        val viewModel = constructor.newInstance(application)
        assertNotNull(viewModel)
    }

    @Test
    fun `viewModel exposes isConnected connectionType isWifi and isCellular flows`() = runTest {
        val fakeInfo = NetworkInfo(
            connectionType = ConnectionType.WIFI,
            isConnected = true,
            isInternetValidated = true,
            isMetered = false,
            downstreamBandwidthKbps = 100000,
            upstreamBandwidthKbps = 50000
        )

        val fakeRepository = object : NetworkRepository {
            override val networkStatusFlow: Flow<NetworkInfo> = flowOf(fakeInfo)
            override fun getCurrentNetworkInfo(): NetworkInfo = fakeInfo
        }

        val viewModel = NetworkViewModel(application, fakeRepository)

        val connected = viewModel.isConnected.first()
        val type = viewModel.connectionType.first()
        val wifi = viewModel.isWifi.first()
        val cellular = viewModel.isCellular.first()
        val uiState = viewModel.uiState.first()

        assertTrue(connected)
        assertEquals(ConnectionType.WIFI, type)
        assertTrue(wifi)
        assertFalse(cellular)
        assertEquals(fakeInfo, uiState.networkInfo)
    }

    @Test
    fun `viewModel handles cellular connection`() = runTest {
        val fakeInfo = NetworkInfo(
            connectionType = ConnectionType.CELLULAR,
            isConnected = true,
            isInternetValidated = false,
            isMetered = true,
            downstreamBandwidthKbps = 25000,
            upstreamBandwidthKbps = 10000
        )

        val fakeRepository = object : NetworkRepository {
            override val networkStatusFlow: Flow<NetworkInfo> = flowOf(fakeInfo)
            override fun getCurrentNetworkInfo(): NetworkInfo = fakeInfo
        }

        val viewModel = NetworkViewModel(application, fakeRepository)

        val cellular = viewModel.isCellular.first()
        val wifi = viewModel.isWifi.first()

        assertTrue(cellular)
        assertFalse(wifi)
    }
}
