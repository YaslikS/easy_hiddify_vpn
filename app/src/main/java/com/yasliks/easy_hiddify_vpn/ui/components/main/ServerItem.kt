package com.yasliks.easy_hiddify_vpn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yas.database.data.ServerDTO
import com.yasliks.easy_hiddify_vpn.R
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun ServerItem(
    server: ServerDTO,
    isSelected: Boolean,
    isCurrentConnected: Boolean,
    isVpnActive: Boolean,
    onConnectClick: () -> Unit,
    onEditClick: () -> Unit,
    onCopyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var itemSize by remember { mutableStateOf(IntSize.Zero) }
    val offsetX = remember(server.id) { Animatable(0f) }
    var isDragging by remember(server.id) { mutableStateOf(false) }
    var showDeleteDialog by remember(server.id) { mutableStateOf(false) }

    val currentConnected by rememberUpdatedState(isCurrentConnected)

    LaunchedEffect(isCurrentConnected) {
        if (isCurrentConnected && offsetX.value > 0f) {
            isDragging = false
            offsetX.animateTo(0f, spring())
        }
    }

    val initials = remember(server.name) {
        server.name.trim().take(2).uppercase().ifEmpty { "SV" }
    }

    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
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

    val itemBgColor = remember(isCurrentConnected, isSelected, isVpnActive, primaryColor) {
        when {
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

    val isSwipingLeft by remember {
        derivedStateOf { offsetX.value < -1f }
    }
    val isSwipingRight by remember {
        derivedStateOf { offsetX.value > 1f }
    }
    val isSwiping = isSwipingLeft || isSwipingRight

    val activeRowBgColor = remember(isSwiping, itemBgColor, isVpnActive, surfaceVariantColor) {
        if (isSwiping && itemBgColor == Color.Transparent) {
            if (isVpnActive) Color.White.copy(alpha = 0.08f) else surfaceVariantColor.copy(alpha = 0.5f)
        } else {
            itemBgColor
        }
    }

    val rowShape = remember(isSwipingLeft, isSwipingRight) {
        when {
            isSwipingLeft -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            isSwipingRight -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
            else -> RectangleShape
        }
    }

    if (showDeleteDialog) {
        val dismissDeleteDialog = {
            showDeleteDialog = false
            scope.launch {
                offsetX.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(),
                )
            }
        }

        AlertDialog(
            onDismissRequest = { dismissDeleteDialog() },
            title = {
                Text(text = stringResource(R.string.delete_server_dialog_title))
            },
            text = {
                Text(text = stringResource(R.string.delete_server_dialog_desc, server.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            val width = itemSize.width.toFloat()
                            if (width > 0f) {
                                offsetX.animateTo(
                                    targetValue = -width,
                                    animationSpec = tween(durationMillis = 180),
                                )
                            }
                            onDeleteClick()
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = errorColor,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { dismissDeleteDialog() }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { itemSize = it }
            .draggable(
                orientation = Orientation.Horizontal,
                onDragStarted = {
                    isDragging = true
                },
                state = rememberDraggableState { delta ->
                    if (isDragging) {
                        scope.launch(start = CoroutineStart.UNDISPATCHED) {
                            val maxSwipe = itemSize.width.toFloat().coerceAtLeast(1f)
                            val maxRightSwipe = if (currentConnected) 0f else maxSwipe
                            val newOffset =
                                (offsetX.value + delta).coerceIn(-maxSwipe, maxRightSwipe)
                            offsetX.snapTo(newOffset)
                        }
                    }
                },
                onDragStopped = { velocity ->
                    isDragging = false
                    scope.launch {
                        val width = itemSize.width.toFloat()
                        if (width > 0f) {
                            val currentOffset = offsetX.value
                            val shouldDelete = currentOffset < 0f &&
                                    ((-currentOffset) > width * 0.3f || velocity < -1500f)
                            val shouldEdit = !currentConnected && currentOffset > 0f &&
                                    (currentOffset > width * 0.3f || velocity > 1500f)

                            when {
                                shouldDelete -> {
                                    val holdOffset = -with(density) { 96.dp.toPx() }
                                        .coerceAtMost(width * 0.4f)
                                    offsetX.animateTo(
                                        targetValue = holdOffset,
                                        animationSpec = spring(),
                                    )
                                    showDeleteDialog = true
                                }

                                shouldEdit -> {
                                    onEditClick()
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(),
                                    )
                                }

                                else -> {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(),
                                    )
                                }
                            }
                        } else {
                            offsetX.snapTo(0f)
                        }
                    }
                },
            ),
    ) {
        if (isSwipingRight && !isCurrentConnected && itemSize.height > 0) {
            val gapPx = with(density) { 8.dp.toPx() }
            val startPaddingPx = with(density) { 6.dp.toPx() }
            val pillWidthDp = with(density) {
                (offsetX.value - gapPx - startPaddingPx).coerceAtLeast(0f).toDp()
            }
            val pillHeightDp = with(density) {
                (itemSize.height.toDp() - 8.dp).coerceAtLeast(0.dp)
            }

            if (pillWidthDp > 0.dp) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp)
                        .size(width = pillWidthDp, height = pillHeightDp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF2196F3)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (pillWidthDp >= 28.dp) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }

        if (isSwipingLeft && itemSize.height > 0) {
            val gapPx = with(density) { 8.dp.toPx() }
            val endPaddingPx = with(density) { 6.dp.toPx() }
            val pillWidthDp = with(density) {
                ((-offsetX.value) - gapPx - endPaddingPx).coerceAtLeast(0f).toDp()
            }
            val pillHeightDp = with(density) {
                (itemSize.height.toDp() - 8.dp).coerceAtLeast(0.dp)
            }

            if (pillWidthDp > 0.dp) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                        .size(width = pillWidthDp, height = pillHeightDp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFEF5350)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (pillWidthDp >= 28.dp) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .clip(rowShape)
                .background(activeRowBgColor)
                .clickable(onClick = onConnectClick)
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

            Spacer(modifier = Modifier.width(4.dp))

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