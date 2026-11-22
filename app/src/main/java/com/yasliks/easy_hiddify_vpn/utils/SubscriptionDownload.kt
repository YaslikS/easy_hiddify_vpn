package com.yasliks.easy_hiddify_vpn.utils

import com.yas.database.data.SubscriptionDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.URI
import java.util.Base64
import javax.net.ssl.HttpsURLConnection

internal object SubscriptionDownload {
    private const val TIMEOUT_MS = 10_000
    private const val MAX_REQUESTS = 6

    data class Content(
        val text: String,
        val subscription: SubscriptionDTO,
    )

    internal fun parseTitle(header: String?): String? {
        val value = header?.trim()?.takeIf { it.isNotEmpty() && it.length <= 1024 } ?: return null
        val title = if (value.startsWith("base64:", ignoreCase = true)) {
            val encoded = value.substringAfter(':').trim()
            sequenceOf(Base64.getDecoder(), Base64.getUrlDecoder()).firstNotNullOfOrNull { decoder ->
                runCatching { decoder.decode(encoded).decodeToString(throwOnInvalidSequence = true) }.getOrNull()
            } ?: return null
        } else value
        return title.filterNot { it.isISOControl() }.trim().take(120).takeIf { it.isNotEmpty() }
    }

    internal fun fallbackTitle(url: String): String? {
        val uri = runCatching { URI(url) }.getOrNull() ?: return null
        return uri.path?.substringAfterLast('/')?.takeIf { it.endsWith(".txt", ignoreCase = true) }
            ?.dropLast(4)?.let { parseTitle(header = it) } ?: uri.host
    }

    internal fun parseExpiry(header: String?): Long? = header?.split(';')
        ?.firstOrNull { it.substringBefore('=').trim().equals("expire", ignoreCase = true) }
        ?.substringAfter('=', "")?.trim()?.toLongOrNull()
        ?.takeIf { it in 1..253402300799L }

    fun parseUrl(input: String): URI = URI(input.trim()).also {
        require(it.scheme.equals("https", ignoreCase = true) && it.host != null)
        require(it.rawUserInfo == null && it.rawFragment == null)
    }.normalize()

    suspend fun fetch(source: URI): Content = withContext(Dispatchers.IO) {
        var url = source
        repeat(times = MAX_REQUESTS) {
            val connection = url.toURL().openConnection() as HttpsURLConnection
            try {
                connection.instanceFollowRedirects = false
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                when (connection.responseCode) {
                    301, 302, 303, 307, 308 -> {
                        url = parseUrl(
                            input = url.resolve(requireNotNull(connection.getHeaderField("Location"))).toString(),
                        )
                    }
                    200 -> {
                        return@withContext Content(
                            text = connection.readSubscriptionBody(),
                            subscription = SubscriptionDTO(
                                url = source.toString(),
                                expiresAt = parseExpiry(header = connection.getHeaderField("Subscription-Userinfo")),
                                title = parseTitle(header = connection.getHeaderField("Profile-Title")),
                            ),
                        )
                    }
                    else -> error("Subscription download failed")
                }
            } finally {
                connection.disconnect()
            }
        }
        error("Too many redirects")
    }

    private fun HttpsURLConnection.readSubscriptionBody(): String {
        require(contentLength <= ImportParser.MAX_BYTES)
        return inputStream.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(size = 8192)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                require(output.size() + read <= ImportParser.MAX_BYTES)
                output.write(buffer, 0, read)
            }
            output.toByteArray().decodeToString(throwOnInvalidSequence = true)
        }
    }
}
