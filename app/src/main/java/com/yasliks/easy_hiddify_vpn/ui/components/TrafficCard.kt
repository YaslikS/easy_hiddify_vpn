package com.yasliks.easy_hiddify_vpn.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TrafficCard(
    title: String,
    speed: String,
    total: String,
    icon: ImageVector,
    iconTint: Color,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(if (isConnected) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), tween(1000), label = "")
    val textColor by animateColorAsState(if (isConnected) Color.White else MaterialTheme.colorScheme.onSurface, tween(1000), label = "")
    val titleColor by animateColorAsState(if (isConnected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant, tween(1000), label = "")
    val currentIconTint by animateColorAsState(if (isConnected) Color.White else iconTint, tween(1000), label = "")

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = currentIconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = titleColor
                )
            }

            Text(
                text = speed,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                color = textColor
            )

            Text(
                text = "Total: $total",
                style = MaterialTheme.typography.labelMedium,
                color = titleColor
            )
        }
    }
}