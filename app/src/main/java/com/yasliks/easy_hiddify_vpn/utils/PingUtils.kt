package com.yasliks.easy_hiddify_vpn.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import androidx.core.net.toUri

class PingUtils @Inject constructor() {

    suspend fun pingConfig(configStr: String): Long = withContext(Dispatchers.IO) {
        val hostAndPort = extractHostAndPort(configStr) ?: return@withContext -1L
        val (host, port) = hostAndPort

        val start = System.currentTimeMillis()
        return@withContext try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 3000)
            }
            val end = System.currentTimeMillis()
            end - start
        } catch (e: Exception) {
            -1L
        }
    }

    private fun extractHostAndPort(config: String): Pair<String, Int>? {
        val trimmed = config.trim()

        if (trimmed.startsWith("{")) {
            try {
                val json = JSONObject(trimmed)
                val outbounds = json.optJSONArray("outbounds")
                if (outbounds != null && outbounds.length() > 0) {
                    val firstProxy = outbounds.getJSONObject(0)
                    val server = firstProxy.optString("server")
                    val port = firstProxy.optInt("server_port", 443)
                    if (server.isNotEmpty()) return Pair(server, port)
                }
            } catch (e: Exception) {
            }
        }

        try {
            val uri = trimmed.toUri()
            val host = uri.host
            val port = if (uri.port > 0) uri.port else 443
            if (!host.isNullOrEmpty()) {
                return Pair(host, port)
            }
        } catch (e: Exception) {
        }

        return null
    }
}