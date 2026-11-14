package com.yasliks.easy_hiddify_vpn.ui.components.main

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yasliks.easy_hiddify_vpn.R

@Composable
fun VpnButton(
    isConnected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val targetColor = remember(
        key1 = isConnected,
        key2 = surfaceVariantColor,
    ) {
        if (isConnected) Color.White else surfaceVariantColor
    }

    val iconColor = remember(
        key1 = isConnected,
        key2 = onSurfaceVariantColor,
    ) {
        if (isConnected) Color(0xFF4CAF50) else onSurfaceVariantColor
    }

    val pulseTarget = remember(isConnected) {
        if (isConnected) 1.15f else 1f
    }

    val middleRingScale = remember(isConnected) {
        if (isConnected) 1.05f else 1f
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(1000),
        label = "buttonColor",
    )

    val outerRingColor by remember {
        derivedStateOf { animatedColor.copy(alpha = 0.2f) }
    }

    val middleRingColor by remember {
        derivedStateOf { animatedColor.copy(alpha = 0.4f) }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = pulseTarget,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(136.dp),
    ) {
        Box(
            modifier = Modifier
                .size(106.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(outerRingColor)
        )

        Box(
            modifier = Modifier
                .size(106.dp)
                .scale(middleRingScale)
                .clip(CircleShape)
                .background(middleRingColor)
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .background(animatedColor)
                .clickable { onClick() },
        ) {
            Icon(
                imageVector = Icons.Rounded.PowerSettingsNew,
                contentDescription = stringResource(R.string.cd_power),
                tint = iconColor,
                modifier = Modifier.size(42.dp),
            )
        }
    }
}