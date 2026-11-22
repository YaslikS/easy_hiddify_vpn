package com.yasliks.easy_hiddify_vpn.ui.components

import androidx.compose.runtime.Composable
import com.yasliks.easy_hiddify_vpn.ui.components.common.ToggleListItem

@Composable
fun SettingSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ToggleListItem(
        title = title,
        subtitle = subtitle,
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}
