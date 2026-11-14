package com.yasliks.easy_hiddify_vpn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServerItem(
    server: ServerDTO,
    isSelected: Boolean,
    isCurrentConnected: Boolean,
    isVpnActive: Boolean,
    isEditMode: Boolean,
    onConnectClick: () -> Unit,
    onEditClick: () -> Unit,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val initials = remember(server.name) {
        server.name.trim().take(2).uppercase().ifEmpty { "SV" }
    }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorContainerColor = MaterialTheme.colorScheme.errorContainer
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val onSecondaryContainerColor = MaterialTheme.colorScheme.onSecondaryContainer
    val errorColor = MaterialTheme.colorScheme.error

    val targetTextColorPrimary = remember(isVpnActive, onSurface) {
        if (isVpnActive) Color.White else onSurface
    }

    val targetTextColorSecondary = remember(isVpnActive, onSurfaceVariant) {
        if (isVpnActive) Color.White.copy(alpha = 0.8f) else onSurfaceVariant
    }

    val textColorPrimary by animateColorAsState(
        targetValue = targetTextColorPrimary,
        animationSpec = tween(1000),
        label = "textColorPrimary",
    )
    val textColorSecondary by animateColorAsState(
        targetValue = targetTextColorSecondary,
        animationSpec = tween(1000),
        label = "textColorSecondary",
    )

    val itemBgColor = remember(
        isEditMode,
        isCurrentConnected,
        isSelected,
        isVpnActive,
        errorContainerColor,
        primaryColor
    ) {
        when {
            isEditMode -> errorContainerColor.copy(alpha = 0.2f)
            isCurrentConnected -> Color(0xFF4CAF50).copy(alpha = 0.2f)
            isSelected -> if (isVpnActive) Color.White.copy(alpha = 0.1f) else primaryColor.copy(
                alpha = 0.15f
            )

            else -> Color.Transparent
        }
    }

    val avatarBgColor = remember(
        isCurrentConnected,
        isSelected,
        isVpnActive,
        primaryColor,
        secondaryContainerColor
    ) {
        when {
            isCurrentConnected -> Color.White
            isSelected -> if (isVpnActive) Color.White.copy(alpha = 0.3f) else primaryColor
            else -> if (isVpnActive) Color.White.copy(alpha = 0.15f) else secondaryContainerColor
        }
    }

    val avatarTextColor = remember(
        isCurrentConnected,
        isSelected,
        isVpnActive,
        onPrimaryColor,
        onSecondaryContainerColor
    ) {
        when {
            isCurrentConnected -> Color(0xFF4CAF50)
            isSelected -> if (isVpnActive) Color.White else onPrimaryColor
            else -> if (isVpnActive) Color.White.copy(alpha = 0.7f) else onSecondaryContainerColor
        }
    }

    val badgeColor = remember(isVpnActive, primaryColor) {
        if (isVpnActive) Color.White.copy(alpha = 0.9f) else primaryColor
    }

    val pingColor by remember(server.pingMs) {
        derivedStateOf {
            when {
                server.pingMs in 0..150 -> Color(0xFF4CAF50)
                server.pingMs in 151..300 -> Color(0xFFFF9800)
                server.pingMs > 300 -> Color(0xFFF44336)
                else -> textColorSecondary
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(itemBgColor)
            .combinedClickable(
                onClick = { if (isEditMode) onEditClick() else onConnectClick() },
                onLongClick = onLongClick,
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(avatarBgColor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = avatarTextColor,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColorPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )

                if (isCurrentConnected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.connected),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                } else if (isSelected) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.selected_server),
                        color = badgeColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = server.config,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = textColorSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                if (!isEditMode) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            server.pingMs == -1L -> stringResource(R.string.ping_unknown)
                            server.pingMs > 3000L -> stringResource(R.string.ping_timeout)
                            else -> stringResource(R.string.ping_ms, server.pingMs)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = pingColor,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        if (isEditMode) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = errorColor,
                    modifier = Modifier.size(20.dp),
                )
            }
        } else {
            IconButton(
                onClick = onCopyClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = textColorPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}