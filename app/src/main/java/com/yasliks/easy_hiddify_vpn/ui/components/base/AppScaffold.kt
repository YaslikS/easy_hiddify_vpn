package com.yasliks.easy_hiddify_vpn.ui.components.base

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration

@Composable
internal fun AppScaffold(
    currentRoute: AppRoute,
    isConnected: Boolean,
    onNavigate: (AppRoute) -> Unit,
    content: @Composable () -> Unit,
) {
    val showRail = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!showRail) {
                AppNavigation(
                    currentRoute = currentRoute,
                    isConnected = isConnected,
                    showRail = false,
                    onNavigate = onNavigate,
                )
            }
        },
    ) { padding ->
        Row(modifier = Modifier.fillMaxSize()) {
            if (showRail) {
                AppNavigation(
                    currentRoute = currentRoute,
                    isConnected = isConnected,
                    showRail = true,
                    onNavigate = onNavigate,
                )
            }
            Box(
                modifier = Modifier.weight(1f).then(
                    if (showRail) {
                        Modifier.windowInsetsPadding(
                            insets = WindowInsets.systemBars.only(sides = WindowInsetsSides.End),
                        )
                    } else {
                        Modifier.padding(bottom = padding.calculateBottomPadding())
                    },
                ),
            ) { content() }
        }
    }
}
