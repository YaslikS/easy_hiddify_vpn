package com.yasliks.easy_hiddify_vpn

import com.yasliks.database.data.SettingsData
import com.yasliks.easy_hiddify_vpn.utils.serverGroupKey
import com.yasliks.easy_hiddify_vpn.utils.withToggledServerGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ServerGroupSettingsTest {
    @Test
    fun groupKeysAreIndependentAndDoNotExposeSubscriptionUrl() {
        val url = "https://example.org/sub?token=private-test-token"
        val key = serverGroupKey(subscriptionUrl = url)

        assertEquals(key, serverGroupKey(subscriptionUrl = url))
        assertNotEquals(key, serverGroupKey(subscriptionUrl = ""))
        assertNotEquals(key, serverGroupKey(subscriptionUrl = "https://example.org/other"))
        assertFalse(key.contains(other = "private-test-token"))
    }

    @Test
    fun restoresCollapsedGroupsWithoutChangingOtherSettings() {
        val manual = serverGroupKey(subscriptionUrl = "")
        val subscription = serverGroupKey(subscriptionUrl = "https://example.org/sub")
        val settings = SettingsData.newBuilder().setLastServerId(42).setAutoStartVpn(true).build()
        val changed = settings.withToggledServerGroup(key = manual).withToggledServerGroup(key = subscription)
        val restored = SettingsData.parseFrom(changed.toByteArray())

        assertEquals(changed, restored)
        assertEquals(
            listOf(element = subscription),
            restored.withToggledServerGroup(key = manual).collapsedServerGroupKeysList,
        )
        assertEquals(42, restored.lastServerId)
        assertEquals(true, restored.autoStartVpn)
    }
}
