package com.yasliks.easy_hiddify_vpn.ui.components.base

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable

@Composable
fun ScreenWrapper(
    title: String = "",
    showTopBar: Boolean = true,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            if (showTopBar) {
                TopBar(
                    title = title,
                    navigationIcon = navigationIcon,
                    actions = actions
                )
            }
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}