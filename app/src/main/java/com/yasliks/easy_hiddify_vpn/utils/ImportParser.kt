package com.yasliks.easy_hiddify_vpn.utils

import com.yas.database.data.ServerDTO
import com.yasliks.easy_hiddify_vpn.data.ImportPreview
import com.yasliks.hiddify_library_lib.prefs.HiddifyPrefs
import kotlinx.collections.immutable.toImmutableList
import java.net.URI
import java.util.Base64

internal object ImportParser {
    const val MAX_BYTES = 2 * 1024 * 1024
    private val schemes = setOf(
        HiddifyPrefs.VLESS_START_CONFIG,
        HiddifyPrefs.SHADOWSOCKS_START_CONFIG,
        HiddifyPrefs.TROJAN_START_CONFIG,
        HiddifyPrefs.HYSTERIA2_START_CONFIG,
        HiddifyPrefs.HY2_START_CONFIG,
    )

    fun parse(input: String): ImportPreview {
        require(input.length <= MAX_BYTES)
        val text = input.trim().removePrefix("\uFEFF").trim()
        val decoded = if (text.contains(other = "://")) text else decode(text = text) ?: text
        val servers = linkedMapOf<String, ServerDTO>()
        var skipped = 0
        decoded.lineSequence().map { it.trim().removePrefix("\uFEFF") }
            .filter { it.isNotBlank() && !it.startsWith('#') }
            .forEach { line ->
                val server = parseServer(line = line)
                if (server == null || servers.putIfAbsent(server.config.substringBefore('#'), server) != null) {
                    skipped++
                }
            }
        return ImportPreview(
            servers = servers.values.toImmutableList(),
            skipped = skipped,
        )
    }

    private fun parseServer(line: String): ServerDTO? {
        val uri = runCatching {
            URI(
                line.substringBefore(delimiter = '#') +
                    if ('#' in line) "#" + line.substringAfter(delimiter = '#').replace(
                        oldValue = " ",
                        newValue = "%20",
                    ) else "",
            )
        }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase()?.takeIf { it in schemes } ?: return null
        val host = uri.host ?: return null
        if (uri.userInfo.isNullOrBlank() ||
            (uri.port != -1 && uri.port !in 1..65535) ||
            (scheme == HiddifyPrefs.SHADOWSOCKS_START_CONFIG && uri.port == -1)
        ) return null
        return ServerDTO(
            name = uri.fragment?.takeIf { it.isNotBlank() } ?: host,
            config = scheme + uri.toString().substring(startIndex = uri.scheme.length),
        )
    }

    private fun decode(text: String): String? {
        val compact = text.filterNot { it.isWhitespace() }
        return sequenceOf(Base64.getDecoder(), Base64.getUrlDecoder())
            .mapNotNull { decoder ->
                runCatching { decoder.decode(compact).decodeToString(throwOnInvalidSequence = true) }
                    .getOrNull()
            }
            .firstOrNull { it.contains("://") }
    }
}
