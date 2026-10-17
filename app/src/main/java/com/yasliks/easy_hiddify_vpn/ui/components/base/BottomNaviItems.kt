package com.yasliks.easy_hiddify_vpn.ui.components.base

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.yasliks.easy_hiddify_vpn.R


sealed class BottomNavItem(
    val appRoute: AppRoute,
    @StringRes val stringId: Int,
    val icon: ImageVector,
) {
    data object MainBottomItem : BottomNavItem(
        appRoute = AppRoute.Main,
        stringId = R.string.main,
        icon = Icons.Default.NetworkCheck,
    )

    data object ApplicationsBottomItem : BottomNavItem(
        appRoute = AppRoute.Apps,
        stringId = R.string.apps,
        icon = Icons.Default.Apps,
    )

    data object SettingsBottomItem : BottomNavItem(
        appRoute = AppRoute.Settings,
        stringId = R.string.settings,
        icon = Icons.Default.Settings,
    )
}