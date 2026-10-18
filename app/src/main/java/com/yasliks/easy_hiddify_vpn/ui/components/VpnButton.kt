package com.yasliks.easy_hiddify_vpn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VpnButton(
    isConnected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetColor = if (isConnected) Color.White else MaterialTheme.colorScheme.surfaceVariant
    val iconColor = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(1000),
        label = "buttonColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(180.dp)
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(animatedColor.copy(alpha = 0.2f))
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(if (isConnected) 1.05f else 1f)
                .clip(CircleShape)
                .background(animatedColor.copy(alpha = 0.4f))
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(animatedColor)
                .clickable { onClick() }
        ) {
            Icon(
                imageVector = Icons.Rounded.PowerSettingsNew,
                contentDescription = "Power",
                tint = iconColor,
                modifier = Modifier.size(56.dp)
            )
        }
    }
}