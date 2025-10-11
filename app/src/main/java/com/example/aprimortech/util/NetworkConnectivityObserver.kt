package com.example.aprimortech.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Monitor de conectividade de rede
 * Fornece um Flow que emite true/false conforme o estado da conexão
 */
class NetworkConnectivityObserver(private val context: Context) {

    companion object {
        private const val TAG = "NetworkConnectivity"
    }

    /**
     * Observa mudanças na conectividade de rede
     * @return Flow<Boolean> - true quando online, false quando offline
     */
    fun observe(): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(TAG, "✅ Conexão disponível")
                trySend(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                Log.d(TAG, "⚠️ Conexão perdida")
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                Log.d(TAG, "🔄 Capacidades da rede mudaram - Internet: $hasInternet, Validada: $isValidated")
                trySend(hasInternet && isValidated)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Envia estado inicial
        val isConnected = isNetworkConnected()
        trySend(isConnected)
        Log.d(TAG, "📊 Estado inicial da conexão: ${if (isConnected) "Online" else "Offline"}")

        awaitClose {
            Log.d(TAG, "🔌 Desregistrando monitor de conectividade")
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    /**
     * Verifica se há conexão de rede no momento
     */
    fun isNetworkConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Verifica o tipo de conexão atual
     */
    fun getConnectionType(): ConnectionType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return ConnectionType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectionType.NONE

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            else -> ConnectionType.UNKNOWN
        }
    }

    enum class ConnectionType {
        WIFI,
        CELLULAR,
        ETHERNET,
        UNKNOWN,
        NONE
    }
}

