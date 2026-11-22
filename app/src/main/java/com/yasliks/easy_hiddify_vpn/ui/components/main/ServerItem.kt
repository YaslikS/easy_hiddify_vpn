package com.yasliks.easy_hiddify_vpn.ui.components.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yas.database.data.ServerDTO
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.ui.components.common.ListItemRow
import com.yasliks.easy_hiddify_vpn.utils.LatencyStatus

internal data class ServerItemActions(
    val onConnect: () -> Unit,
    val onEdit: () -> Unit,
    val onCopy: () -> Unit,
    val onDelete: () -> Unit,
)

@Composable
internal fun ServerItem(
    server: ServerDTO,
    actions: ServerItemActions? = null,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isCurrentConnected: Boolean = false,
    isVpnActive: Boolean = false,
    isPinging: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val textColor by animateColorAsState(
        targetValue = if (isVpnActive) Color.White else colors.onSurface,
        animationSpec = tween(durationMillis = 1000),
        label = "serverTextColor",
    )
    val secondaryTextColor by animateColorAsState(
        targetValue = if (isVpnActive) Color.White.copy(alpha = 0.8f) else colors.onSurfaceVariant,
        animationSpec = tween(durationMillis = 1000),
        label = "serverSecondaryTextColor",
    )

    SwipeActionRow(
        isConnected = isVpnActive,
        primaryActionIcon = Icons.Outlined.Edit,
        onPrimaryAction = { actions?.onEdit?.invoke() },
        onDelete = { actions?.onDelete?.invoke() },
        deleteTitle = stringResource(id = R.string.delete_server_dialog_title),
        deleteMessage = stringResource(R.string.delete_server_dialog_desc, server.name),
        modifier = modifier,
        enabled = actions != null,
        primaryActionEnabled = !isCurrentConnected,
        backgroundColor = when {
            isCurrentConnected -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            isSelected && isVpnActive -> Color.White.copy(alpha = 0.1f)
            isSelected -> colors.primary.copy(alpha = 0.15f)
            else -> Color.Transparent
        },
    ) {
        ListItemRow(
            modifier = if (actions == null) Modifier else Modifier.clickable(onClick = actions.onConnect),
            leading = {
                ServerAvatar(
                    name = server.name,
                    isSelected = isSelected,
                    isCurrentConnected = isCurrentConnected,
                    isVpnActive = isVpnActive,
                )
            },
            trailingSpacing = 4.dp,
            trailing = if (actions == null) null else {
                {
                    IconButton(
                        onClick = actions.onCopy,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            },
        ) {
            Column {
                ServerItemTitle(
                    name = server.name,
                    isSelected = isSelected,
                    isCurrentConnected = isCurrentConnected,
                    isVpnActive = isVpnActive,
                    color = textColor,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = server.config,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = secondaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    ServerLatencyIndicator(
                        latency = server.pingMs,
                        isPinging = isPinging,
                        color = secondaryTextColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerAvatar(
    name: String,
    isSelected: Boolean,
    isCurrentConnected: Boolean,
    isVpnActive: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    val fallbackName = stringResource(id = R.string.server_name)
    val initials = remember(name, fallbackName) {
        name.trim().ifEmpty { fallbackName }.take(n = 2).uppercase()
    }
    Box(
        modifier = Modifier.size(38.dp)
            .clip(CircleShape)
            .background(
                when {
                    isCurrentConnected -> Color.White
                    isSelected && isVpnActive -> Color.White.copy(alpha = 0.3f)
                    isSelected -> colors.primary
                    isVpnActive -> Color.White.copy(alpha = 0.15f)
                    else -> colors.secondaryContainer
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = when {
                isCurrentConnected -> Color(0xFF4CAF50)
                isSelected && isVpnActive -> Color.White
                isSelected -> colors.onPrimary
                isVpnActive -> Color.White.copy(alpha = 0.7f)
                else -> colors.onSecondaryContainer
            },
        )
    }
}

@Composable
private fun ServerItemTitle(
    name: String,
    isSelected: Boolean,
    isCurrentConnected: Boolean,
    isVpnActive: Boolean,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(weight = 1f, fill = false),
        )
        if (isCurrentConnected || isSelected) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(id = if (isCurrentConnected) R.string.connected else R.string.selected_server),
                color = when {
                    isCurrentConnected -> Color.White
                    isVpnActive -> Color.White.copy(alpha = 0.9f)
                    else -> MaterialTheme.colorScheme.primary
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ServerLatencyIndicator(
    latency: Long,
    isPinging: Boolean,
    color: Color,
) {
    if (isPinging) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            color = color,
            strokeWidth = 2.dp,
        )
    } else {
        Text(
            text = when (latency) {
                LatencyStatus.UNTESTED -> stringResource(id = R.string.ping_unknown)
                LatencyStatus.NO_RESPONSE -> stringResource(id = R.string.ping_timeout)
                LatencyStatus.FAILED -> stringResource(id = R.string.ping_failed)
                else -> stringResource(R.string.ping_ms, latency)
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = when (latency) {
                in 0..150 -> Color(0xFF4CAF50)
                in 151..300 -> Color(0xFFFF9800)
                in 301..Long.MAX_VALUE -> Color(0xFFF44336)
                else -> color
            },
        )
    }
}
