package com.example.glamora.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart

/**
 * Sealed class representing the current network connection status.
 */
sealed class ConnectionState {
    object Available : ConnectionState()
    object Unavailable : ConnectionState()
}

/**
 * Monitors the network connectivity status of the device.
 * Used by the ViewModel to provide real-time connection status to the UI.
 */
class NetworkMonitor(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Checks the initial network state.
     */
    private fun getCurrentConnectivityState(): ConnectionState {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return if (capabilities != null &&
            (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))) {
            ConnectionState.Available
        } else {
            ConnectionState.Unavailable
        }
    }

    /**
     * Provides a Flow of the current network connection status.
     * The connection status is emitted whenever it changes.
     */
    val isConnected: Flow<ConnectionState> = callbackFlow {
        // Send initial state immediately
        trySend(getCurrentConnectivityState())

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(ConnectionState.Available)
                Log.d("NetworkMonitor", "Network Available")
            }

            override fun onLost(network: Network) {
                trySend(ConnectionState.Unavailable)
                Log.d("NetworkMonitor", "Network Lost")
            }

            // In some cases (e.g., transition from cellular to Wi-Fi), onCapabilitiesChanged
            // might be the first indication of a successful connection.
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    trySend(ConnectionState.Available)
                }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, networkCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
        // Only emit when the state actually changes
        .distinctUntilChanged()
}