package com.example.trip_planner.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 网络状态监听器
 * 
 * 功能：
 * 1. 实时监听网络连接状态
 * 2. 网络断开时提示用户
 * 3. 提供当前网络状态的 Flow
 */
class NetworkMonitor(context: Context) : DefaultLifecycleObserver {

    companion object {
        /** 网络恢复提示 */
        private const val MSG_NETWORK_CONNECTED = "✅ 网络已连接"
        /** 网络断开提示 */
        private const val MSG_NETWORK_DISCONNECTED = "⚠️ 网络已断开，部分功能可能受限"
    }

    /** 网络连接状态 Flow */
    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    /** 连接管理器 */
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /** 网络回调 */
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
        }
    }

    /** 协程作用域 */
    private var monitorScope: CoroutineScope? = null
    private var monitorJob: kotlinx.coroutines.Job? = null

    /**
     * 开始监听网络状态
     */
    fun startListening(scope: CoroutineScope, snackbarHostState: SnackbarHostState) {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        monitorScope = scope
        monitorJob = scope.launch {
            isConnected.collect { connected ->
                if (!connected) {
                    snackbarHostState.showSnackbar(MSG_NETWORK_DISCONNECTED)
                }
            }
        }
    }

    /**
     * 停止监听网络状态
     */
    fun stopListening() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
            monitorJob?.cancel()
            monitorJob = null
            monitorScope = null
        } catch (e: Exception) {
            // 忽略注销时的异常
        }
    }

    /**
     * 检查当前是否有网络连接
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
