package com.yasliks.easy_hiddify_vpn.utils

import java.net.URI
import java.net.URLDecoder

internal enum class ImportKind {
    AUTO,
    SERVER,
    SERVERS,
    SUBSCRIPTION,
}

internal object ImportDeepLinkParser {
    data class Request(
        val input: String,
        val kind: ImportKind,
    )

    fun parse(link: String?): Request? {
        val uri = link?.let { runCatching { URI(it) }.getOrNull() } ?: return null
        if (uri.scheme != "easyhiddify" || uri.host != "import") return null
        val kind = when (uri.path) {
            "/server" -> ImportKind.SERVER
            "/servers" -> ImportKind.SERVERS
            "/subscription" -> ImportKind.SUBSCRIPTION
            else -> return null
        }
        val parameter = if (kind == ImportKind.SUBSCRIPTION) "url" else "data"
        val value = uri.rawQuery?.splitToSequence(delimiters = charArrayOf('&'))
            ?.firstOrNull { it.substringBefore(delimiter = '=') == parameter }
            ?.substringAfter(delimiter = '=', missingDelimiterValue = "")
            ?: return null
        val input = runCatching { URLDecoder.decode(value, Charsets.UTF_8.name()) }.getOrNull() ?: return null
        if (input.length > ImportParser.MAX_BYTES) return null
        return Request(
            input = input,
            kind = kind,
        )
    }
}
