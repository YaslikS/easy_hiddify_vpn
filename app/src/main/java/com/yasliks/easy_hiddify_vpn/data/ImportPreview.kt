package com.yasliks.easy_hiddify_vpn.data

import com.yas.database.data.ServerDTO
import kotlinx.collections.immutable.ImmutableList

internal data class ImportPreview(
    val servers: ImmutableList<ServerDTO>,
    val skipped: Int,
)
