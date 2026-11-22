package com.yasliks.easy_hiddify_vpn.ui.components.base

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf

private const val NAVIGATION_ANIMATION_MS = 1400
private val ConnectedNavigationColor = Color(0xFF4CAF50)
private val NavigationTabs = persistentListOf(
    BottomNavItem.MainBottomItem,
    BottomNavItem.ApplicationsBottomItem,
    BottomNavItem.SettingsBottomItem,
)

@Composable
internal fun AppNavigation(
    currentRoute: AppRoute,
    isConnected: Boolean,
    showRail: Boolean,
    onNavigate: (AppRoute) -> Unit,
) {
    val palette = MaterialTheme.colorScheme
    val container by animateColorAsState(
        targetValue = if (isConnected) ConnectedNavigationColor else palette.surface,
        animationSpec = tween(durationMillis = NAVIGATION_ANIMATION_MS),
        label = "navigationContainer",
    )
    val indicator by animateColorAsState(
        targetValue = if (isConnected) Color.White else palette.secondaryContainer,
        animationSpec = tween(durationMillis = NAVIGATION_ANIMATION_MS),
        label = "navigationIndicator",
    )
    val selectedIcon by animateColorAsState(
        targetValue = if (isConnected) ConnectedNavigationColor else palette.onSecondaryContainer,
        animationSpec = tween(durationMillis = NAVIGATION_ANIMATION_MS),
        label = "navigationSelectedIcon",
    )
    val selectedText by animateColorAsState(
        targetValue = if (isConnected) Color.White else palette.onSurface,
        animationSpec = tween(durationMillis = NAVIGATION_ANIMATION_MS),
        label = "navigationSelectedText",
    )
    val unselected by animateColorAsState(
        targetValue = if (isConnected) Color.White.copy(alpha = 0.7f) else palette.onSurfaceVariant,
        animationSpec = tween(durationMillis = NAVIGATION_ANIMATION_MS),
        label = "navigationUnselected",
    )

    if (showRail) {
        Surface(color = container) {
            NavigationRail(
                containerColor = Color.Transparent,
                modifier = Modifier.windowInsetsPadding(
                    insets = WindowInsets.systemBars.only(
                        sides = WindowInsetsSides.Vertical + WindowInsetsSides.Start,
                    ),
                ),
            ) {
                NavigationTabs.forEach { tab ->
                    NavigationRailItem(
                        selected = currentRoute.getActiveTab() == tab.appRoute,
                        onClick = { onNavigate(tab.appRoute) },
                        icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                        label = { Text(text = stringResource(id = tab.stringId)) },
                        colors = NavigationRailItemDefaults.colors(
                            indicatorColor = indicator,
                            selectedIconColor = selectedIcon,
                            selectedTextColor = selectedText,
                            unselectedIconColor = unselected,
                            unselectedTextColor = unselected,
                        ),
                    )
                }
            }
        }
    } else {
        NavigationBar(containerColor = container) {
            NavigationTabs.forEach { tab ->
                NavigationBarItem(
                    selected = currentRoute.getActiveTab() == tab.appRoute,
                    onClick = { onNavigate(tab.appRoute) },
                    icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                    label = { Text(text = stringResource(id = tab.stringId)) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = indicator,
                        selectedIconColor = selectedIcon,
                        selectedTextColor = selectedText,
                        unselectedIconColor = unselected,
                        unselectedTextColor = unselected,
                    ),
                )
            }
        }
    }
}
