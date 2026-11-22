package com.yasliks.easy_hiddify_vpn

import com.yasliks.easy_hiddify_vpn.utils.ImportDeepLinkParser
import com.yasliks.easy_hiddify_vpn.data.ImportKind
import com.yasliks.easy_hiddify_vpn.data.ImportRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImportDeepLinkParserTest {
    @Test
    fun parsesSingleServerAndServerList() {
        assertEquals(
            ImportRequest(
                input = "hy2://test@vpn.example:443#Первый",
                kind = ImportKind.SERVER,
            ),
            ImportDeepLinkParser.parse(
                link = "easyhiddify://import/server?data=hy2%3A%2F%2Ftest%40vpn.example%3A443%23Первый",
            ),
        )
        assertEquals(
            ImportRequest(
                input = "first\nsecond",
                kind = ImportKind.SERVERS,
            ),
            ImportDeepLinkParser.parse(link = "easyhiddify://import/servers?data=first%0Asecond"),
        )
    }

    @Test
    fun preservesQueryOfNestedSubscriptionUrl() {
        assertEquals(
            ImportRequest(
                input = "https://vpn.example/list?token=a+b&format=text",
                kind = ImportKind.SUBSCRIPTION,
            ),
            ImportDeepLinkParser.parse(
                link = "easyhiddify://import/subscription?url=https%3A%2F%2Fvpn.example%2Flist" +
                    "%3Ftoken%3Da%2Bb%26format%3Dtext",
            ),
        )
    }

    @Test
    fun rejectsUnsupportedAndMalformedLinks() {
        assertNull(ImportDeepLinkParser.parse(link = "https://import/server?data=example"))
        assertNull(ImportDeepLinkParser.parse(link = "easyhiddify://other/server?data=example"))
        assertNull(ImportDeepLinkParser.parse(link = "easyhiddify://import/unknown?data=example"))
        assertNull(ImportDeepLinkParser.parse(link = "easyhiddify://import/subscription?data=example"))
        assertNull(ImportDeepLinkParser.parse(link = "easyhiddify://import/server?data=%ZZ"))
    }
}
