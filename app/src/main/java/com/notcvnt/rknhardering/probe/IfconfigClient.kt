package com.notcvnt.rknhardering.probe

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Proxy

object IfconfigClient {

    private val ENDPOINTS = listOf(
        "https://ifconfig.me/ip",
        "https://checkip.amazonaws.com",
        "https://ipv4-internet.yandex.net/api/v0/ip",
        "https://ipv6-internet.yandex.net/api/v0/ip",
    )

    suspend fun fetchDirectIp(timeoutMs: Int = 7000): Result<String> =
        fetchIpWithFallback(timeoutMs = timeoutMs)

    suspend fun fetchIpViaProxy(
        endpoint: ProxyEndpoint,
        timeoutMs: Int = 7000,
    ): Result<String> = fetchIpWithFallback(
        timeoutMs = timeoutMs,
        proxy = Proxy(
            when (endpoint.type) {
                ProxyType.SOCKS5 -> Proxy.Type.SOCKS
                ProxyType.HTTP -> Proxy.Type.HTTP
            },
            InetSocketAddress(endpoint.host, endpoint.port),
        ),
    )

    private suspend fun fetchIpWithFallback(
        timeoutMs: Int,
        proxy: Proxy? = null,
    ): Result<String> = withContext(Dispatchers.IO) {
        var lastError: Exception? = null
        for (ep in ENDPOINTS) {
            val result = PublicIpClient.fetchIp(ep, timeoutMs, proxy)
            if (result.isSuccess) return@withContext result
            lastError = result.exceptionOrNull() as? Exception ?: lastError
        }
        Result.failure(lastError ?: IOException("All IP endpoints failed"))
    }
}
