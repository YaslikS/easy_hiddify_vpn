package com.yasliks.easy_hiddify_vpn.ui.components.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun vpnPanelBorder(isConnected: Boolean): BorderStroke {
    val color by animateColorAsState(
        targetValue = if (isConnected) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(durationMillis = 500),
        label = "vpnPanelBorder",
    )
    return BorderStroke(
        width = 1.dp,
        color = color,
    )
}
