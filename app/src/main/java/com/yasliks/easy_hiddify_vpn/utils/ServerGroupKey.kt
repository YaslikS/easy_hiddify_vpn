package com.yasliks.easy_hiddify_vpn.utils

import com.yasliks.database.data.SettingsData
import java.security.MessageDigest
import java.util.Base64

internal fun serverGroupKey(subscriptionUrl: String): String {
    if (subscriptionUrl.isEmpty()) return "manual"
    val digest = MessageDigest.getInstance("SHA-256").digest(subscriptionUrl.encodeToByteArray())
    return "subscription:${Base64.getUrlEncoder().withoutPadding().encodeToString(digest)}"
}

internal fun SettingsData.withToggledServerGroup(key: String): SettingsData = toBuilder()
    .clearCollapsedServerGroupKeys()
    .addAllCollapsedServerGroupKeys(
        if (key in collapsedServerGroupKeysList) collapsedServerGroupKeysList - key
        else collapsedServerGroupKeysList + key,
    )
    .build()
