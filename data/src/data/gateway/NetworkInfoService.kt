/*
 * This file is part of KokoroBox.
 *
 * KokoroBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (c)  AmamiyaKokoro 2025 - Present
 *
 */



package com.amamiyakokoro.box.data.gateway

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.amamiyakokoro.box.core.util.NetworkInterfaces
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Inet6Address
import java.net.InetAddress
import java.util.Locale
import java.util.concurrent.TimeUnit

@Serializable
data class IpInfo(
    val ip: String,
    @SerialName("country_code")
    val countryCode: String? = null,
    val country: String? = null,
) {
    fun normalized(): IpInfo = copy(
        ip = ip.trim(),
        countryCode = (countryCode ?: country)
            ?.trim()
            ?.uppercase(Locale.ROOT)
            ?.takeIf(::isValidCountryCode),
        country = country?.trim(),
    )
}

sealed class IpMonitoringState {
    data class Success(val localIp: String?, val externalIp: IpInfo?, val isProxyActive: Boolean = false) :
        IpMonitoringState()

    data class Error(val message: String) : IpMonitoringState()
    object Loading : IpMonitoringState()
}

class NetworkInfoService(
    private val httpClient: OkHttpClient = SharedOkHttpClient.newBuilder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .callTimeout(5, TimeUnit.SECONDS)
        .build(),
    private val externalIpEndpoints: List<String> = EXTERNAL_IP_ENDPOINTS,
    private val context: Context? = null,
    private val userAgent: String = DEFAULT_USER_AGENT,
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val _refreshTrigger =
        MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun triggerRefresh() {
        _refreshTrigger.tryEmit(Unit)
    }

    suspend fun getLocalIp(): String? {
        return NetworkInterfaces.getLocalIpAddress()
    }

    suspend fun getExternalIp(): IpInfo? {
        for (endpoint in externalIpEndpoints) {
            val info = fetchExternalIp(endpoint)
            if (info != null) {
                return info
            }
        }
        return null
    }

    private suspend fun fetchExternalIp(endpoint: String): IpInfo? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(endpoint)
                .header("Accept", "application/json")
                .header("User-Agent", userAgent)
                .build()
            val info = httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body
                val declaredLength = body.contentLength()
                if (declaredLength > MAX_RESPONSE_BYTES) return@use null
                val source = body.source()
                source.request(MAX_RESPONSE_BYTES + 1L)
                if (source.buffer.size > MAX_RESPONSE_BYTES) return@use null
                json.decodeFromString<IpInfo>(source.readUtf8()).normalized()
            }
            info?.takeIf { isValidIpAddress(it.ip) }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            null
        }
    }

    @OptIn(FlowPreview::class)
    fun startIpMonitoring(
        isProxyActiveFlow: Flow<Boolean>,
        externalRefreshFlow: Flow<Unit> = emptyFlow(),
    ): Flow<IpMonitoringState> {
        var lastSuccessfulState: IpMonitoringState.Success? = null

        val refreshFlow = merge(
            flowOf(Unit),
            _refreshTrigger,
            externalRefreshFlow,
            observeNetworkChanges(),
        ).debounce(NETWORK_CHANGE_DEBOUNCE_MS)

        return combine(refreshFlow, isProxyActiveFlow.distinctUntilChanged()) { _, isProxyActive ->
            isProxyActive
        }.map { isProxyActive ->
            try {
                val localIp = getLocalIp()
                val externalIp = getExternalIp()
                val newState = IpMonitoringState.Success(localIp, externalIp, isProxyActive)
                lastSuccessfulState = newState
                newState
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                lastSuccessfulState?.copy(isProxyActive = isProxyActive)
                    ?: IpMonitoringState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun observeNetworkChanges(): Flow<Unit> {
        val connectivityManager = context?.getSystemService(ConnectivityManager::class.java)
            ?: return emptyFlow()
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            .build()
        return callbackFlow {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) = trigger()

                override fun onLost(network: Network) = trigger()

                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = trigger()

                private fun trigger() {
                    trySend(Unit)
                }
            }
            runCatching { connectivityManager.registerNetworkCallback(request, callback) }
                .onFailure { close(it) }
            awaitClose {
                runCatching { connectivityManager.unregisterNetworkCallback(callback) }
            }
        }.buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    }

    private companion object {
        const val NETWORK_CHANGE_DEBOUNCE_MS = 750L
        const val MAX_RESPONSE_BYTES = 64L * 1024L
        const val DEFAULT_USER_AGENT = "KokoroBox-Android"
    }
}

private fun isValidCountryCode(value: String): Boolean =
    value.length == 2 && value.all { it in 'A'..'Z' }

private fun isValidIpAddress(value: String): Boolean {
    if (value.contains(':')) {
        if (value.any { it !in "0123456789abcdefABCDEF:." }) return false
        return runCatching { InetAddress.getByName(value) is Inet6Address }.getOrDefault(false)
    }
    val parts = value.split('.')
    return parts.size == 4 && parts.all { part ->
        part.isNotEmpty() &&
            part.length <= 3 &&
            part.all(Char::isDigit) &&
            part.toIntOrNull()?.let { it in 0..255 } == true
    }
}

private val EXTERNAL_IP_ENDPOINTS = listOf(
    "https://api.ip.sb/geoip",
    "https://ipapi.co/json/",
    "https://api64.ipify.org?format=json",
)
