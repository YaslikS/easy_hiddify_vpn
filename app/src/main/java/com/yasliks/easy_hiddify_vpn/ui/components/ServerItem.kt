package com.yasliks.easy_hiddify_vpn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yas.database.data.ServerDTO

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
    val initials = server.name.trim().take(2).uppercase().ifEmpty { "SV" }

    val textColorPrimary by animateColorAsState(if (isVpnActive) Color.White else MaterialTheme.colorScheme.onSurface, tween(1000), label = "")
    val textColorSecondary by animateColorAsState(if (isVpnActive) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant, tween(1000), label = "")

    val cardBorder = when {
        isCurrentConnected -> BorderStroke(2.dp, Color.White)
        isSelected -> BorderStroke(2.dp, if (isVpnActive) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary)
        else -> null
    }

    val containerColor = when {
        isEditMode -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        isCurrentConnected -> Color.White.copy(alpha = 0.25f)
        isSelected -> if (isVpnActive) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
        else -> if (isVpnActive) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val avatarBgColor = when {
        isCurrentConnected -> Color.White
        isSelected -> if (isVpnActive) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary
        else -> if (isVpnActive) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer
    }

    val avatarTextColor = when {
        isCurrentConnected -> Color(0xFF4CAF50)
        isSelected -> if (isVpnActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onPrimary
        else -> if (isVpnActive) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = cardBorder,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = { if (isEditMode) onEditClick() else onConnectClick() },
                    onLongClick = onLongClick
                )
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(avatarBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = avatarTextColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColorPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isCurrentConnected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CONNECTED", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    } else if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SELECTED", color = textColorPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = server.config,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColorSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isEditMode) {
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }
            } else {
                IconButton(onClick = onCopyClick) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = textColorPrimary)
                }
            }
        }
    }
}