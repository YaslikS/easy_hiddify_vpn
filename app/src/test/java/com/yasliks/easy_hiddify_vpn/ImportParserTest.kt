package com.yasliks.easy_hiddify_vpn

import com.yasliks.easy_hiddify_vpn.utils.ImportParser
import com.yasliks.easy_hiddify_vpn.utils.SubscriptionDownload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.util.Base64

class ImportParserTest {
    @Test
    fun readsSubscriptionTitleAndFallsBackWithoutQueryParameters() {
        val title = "Моя подписка"
        val encoded = Base64.getEncoder().encodeToString(title.toByteArray())

        assertEquals(title, SubscriptionDownload.parseTitle(header = "base64:$encoded"))
        assertEquals("Servers", SubscriptionDownload.parseTitle(header = " Servers "))
        assertEquals(null, SubscriptionDownload.parseTitle(header = "base64:!"))
        assertEquals("Servers", SubscriptionDownload.fallbackTitle(url = "https://example.org/Servers.txt?token=x"))
        assertEquals("example.org", SubscriptionDownload.fallbackTitle(url = "https://example.org/token"))
    }

    @Test
    fun readsOnlyValidExpirationFromSubscriptionHeader() {
        assertEquals(1_900_000_000L, SubscriptionDownload.parseExpiry(header = "upload=0; Expire = 1900000000"))
        assertEquals(null, SubscriptionDownload.parseExpiry(header = "expire=0"))
        assertEquals(null, SubscriptionDownload.parseExpiry(header = "expire=invalid"))
        assertEquals(null, SubscriptionDownload.parseExpiry(header = null))
    }

    @Test
    fun parsesMixedListAndSkipsDuplicatesAndUnsupportedProtocols() {
        val preview = ImportParser.parse(input = """
            # Header
            vless://id@vpn.example:443#First server
            vless://id@vpn.example:443#Duplicate
            trojan://pass@vpn.example:443#Trojan
            vmess://unsupported
            hy2://pass@vpn.example:443?sni=vpn.example#HY2
            hysteria2://vpn.example#Missing-password
        """.trimIndent())

        assertEquals(listOf("First server", "Trojan", "HY2"), preview.servers.map { it.name })
        assertEquals(3, preview.skipped)
    }

    @Test
    fun parsesWrappedBase64AndUrlSafeBase64() {
        val text = "\uFEFFss://YWVzOnBhc3M@vpn.example:443#🇷🇺"
        val plain = ImportParser.parse(input = text)
        val wrapped = Base64.getEncoder().encodeToString(text.toByteArray()).chunked(12).joinToString("\r\n")
        val urlSafe = Base64.getUrlEncoder().withoutPadding().encodeToString(text.toByteArray())

        assertEquals(1, plain.servers.size)
        assertEquals(plain, ImportParser.parse(input = wrapped))
        assertEquals(plain, ImportParser.parse(input = urlSafe))
    }

    @Test
    fun subscriptionRequiresHttpsAndRejectsCredentials() {
        assertThrows(IllegalArgumentException::class.java) {
            SubscriptionDownload.parseUrl(input = "http://vpn.example/list")
        }
        assertThrows(IllegalArgumentException::class.java) {
            SubscriptionDownload.parseUrl(input = "https://user:password@vpn.example/list")
        }
        assertEquals("vpn.example", SubscriptionDownload.parseUrl(input = "https://vpn.example/list").host)
    }
}
