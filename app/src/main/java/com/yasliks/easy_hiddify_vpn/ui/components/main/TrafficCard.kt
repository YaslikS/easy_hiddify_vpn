package com.yasliks.easy_hiddify_vpn.ui.components.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yasliks.easy_hiddify_vpn.R

@Composable
fun TrafficCard(
    title: String,
    speed: String,
    total: String,
    icon: ImageVector,
    iconTint: Color,
    isConnected: Boolean,
    modifier: Modifier = Modifier,
) {
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val targetBgColor = remember(
        key1 = isConnected,
        key2 = surfaceVariantColor,
    ) {
        if (isConnected) Color.White.copy(alpha = 0.2f) else surfaceVariantColor.copy(alpha = 0.5f)
    }

    val targetTextColor = remember(
        key1 = isConnected,
        key2 = onSurfaceColor,
    ) {
        if (isConnected) Color.White else onSurfaceColor
    }

    val targetTitleColor = remember(
        key1 = isConnected,
        key2 = onSurfaceVariantColor,
    ) {
        if (isConnected) Color.White.copy(alpha = 0.8f) else onSurfaceVariantColor
    }

    val targetIconTint = remember(
        key1 = isConnected,
        key2 = iconTint,
    ) {
        if (isConnected) Color.White else iconTint
    }

    val bgColor by animateColorAsState(targetBgColor, tween(1000), label = "bgColor")
    val textColor by animateColorAsState(targetTextColor, tween(1000), label = "textColor")
    val titleColor by animateColorAsState(targetTitleColor, tween(1000), label = "titleColor")
    val currentIconTint by animateColorAsState(targetIconTint, tween(1000), label = "iconTint")

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = vpnPanelBorder(isConnected = isConnected),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = currentIconTint,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = titleColor,
                )
            }

            Text(
                text = speed,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, bottom = 1.dp),
                color = textColor,
            )

            Text(
                text = stringResource(R.string.total, total),
                style = MaterialTheme.typography.labelSmall,
                color = titleColor,
            )
        }
    }
}
