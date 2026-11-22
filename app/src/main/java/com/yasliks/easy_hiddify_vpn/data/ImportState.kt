package com.yasliks.easy_hiddify_vpn.data

import com.yas.database.data.SubscriptionDTO

internal data class ImportState(
    val input: String = "",
    val kind: ImportKind = ImportKind.AUTO,
    val busy: Boolean = false,
    val error: Boolean = false,
    val subscription: SubscriptionDTO? = null,
    val preview: ImportPreview? = null,
)
