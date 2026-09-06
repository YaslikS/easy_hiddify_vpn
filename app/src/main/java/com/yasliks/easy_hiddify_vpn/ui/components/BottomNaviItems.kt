package com.yasliks.easy_hiddify_vpn.ui.components

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.yasliks.easy_hiddify_vpn.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings


sealed class BottomNavItems(
    val route: String,
    @param:StringRes val stringId: Int,
    val icon: ImageVector,
) {

    /**
     * VPN - main.
     */
    data object MainBottomItem : BottomNavItems(
        route = "screenCatalog",
        stringId = R.string.main,
        Icons.Default.NetworkCheck,
    )

    /**
     * Applications.
     */
    data object ApplicationsBottomItem : BottomNavItems(
        route = "screenCart",
        stringId = R.string.apps,
        Icons.Default.Apps,
    )

    /**
     * Settings.
     */
    data object SettingsBottomItem : BottomNavItems(
        route = "screenSettings",
        stringId = R.string.settings,
        Icons.Default.Settings,
    )
}