package com.yasliks.easy_hiddify_vpn.utils

import com.yasliks.easy_hiddify_vpn.data.ImportKind
import com.yasliks.easy_hiddify_vpn.data.ImportRequest
import java.net.URI
import java.net.URLDecoder

internal object ImportDeepLinkParser {
    fun parse(link: String?): ImportRequest? {
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
        return ImportRequest(
            input = input,
            kind = kind,
        )
    }
}
