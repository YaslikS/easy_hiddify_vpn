package com.yasliks.easy_hiddify_vpn.data

import com.yas.database.data.ServerDTO
import com.yasliks.easy_hiddify_vpn.utils.serverGroupKey
import kotlinx.collections.immutable.ImmutableList

internal data class ServerGroup(
    val subscriptionUrl: String,
    val servers: ImmutableList<ServerDTO>,
) {
    val key: String = serverGroupKey(subscriptionUrl = subscriptionUrl)
}
