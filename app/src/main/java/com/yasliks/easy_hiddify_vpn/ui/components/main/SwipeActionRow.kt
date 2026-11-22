package com.yasliks.easy_hiddify_vpn.ui.components.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.yasliks.easy_hiddify_vpn.ui.components.common.DeleteConfirmationDialog
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun SwipeActionRow(
    isConnected: Boolean,
    primaryActionIcon: ImageVector,
    onPrimaryAction: () -> Unit,
    onDelete: () -> Unit,
    deleteTitle: String,
    deleteMessage: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primaryActionEnabled: Boolean = true,
    backgroundColor: Color = Color.Transparent,
    restingShape: Shape = RectangleShape,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        Box(
            modifier = modifier.fillMaxWidth()
                .clip(shape = restingShape)
                .background(color = backgroundColor),
        ) { content() }
        return
    }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(initialValue = 0f) }
    var rowSize by remember { mutableStateOf(IntSize.Zero) }
    var dragging by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val canSwipeRight by rememberUpdatedState(newValue = primaryActionEnabled)
    LaunchedEffect(key1 = primaryActionEnabled) {
        if (!primaryActionEnabled && offset.value > 0f) {
            dragging = false
            offset.animateTo(targetValue = 0f, animationSpec = spring())
        }
    }
    val reset = remember(scope, offset) {
        {
            showDeleteDialog = false
            scope.launch { offset.animateTo(targetValue = 0f, animationSpec = spring()) }
            Unit
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            title = deleteTitle,
            message = deleteMessage,
            onDismiss = reset,
            onConfirm = {
                showDeleteDialog = false
                scope.launch {
                    offset.animateTo(
                        targetValue = -rowSize.width.toFloat(),
                        animationSpec = tween(durationMillis = 180),
                    )
                    onDelete()
                    offset.animateTo(targetValue = 0f, animationSpec = spring())
                }
            },
        )
    }

    Box(
        modifier = modifier.fillMaxWidth()
            .onSizeChanged { rowSize = it }
            .draggable(
                orientation = Orientation.Horizontal,
                enabled = !showDeleteDialog,
                onDragStarted = { dragging = true },
                state = rememberDraggableState { delta ->
                    if (dragging) {
                        scope.launch(start = CoroutineStart.UNDISPATCHED) {
                            val width = rowSize.width.toFloat()
                            offset.snapTo(targetValue = (offset.value + delta).coerceIn(
                                minimumValue = -width,
                                maximumValue = if (canSwipeRight) width else 0f,
                            ))
                        }
                    }
                },
                onDragStopped = { velocity ->
                    dragging = false
                    scope.launch {
                        val width = rowSize.width.toFloat()
                        when {
                            offset.value < 0 && (-offset.value > width * 0.3f || velocity < -1500f) -> {
                                offset.animateTo(
                                    targetValue = -with(density) { 96.dp.toPx() }.coerceAtMost(width * 0.4f),
                                    animationSpec = spring(),
                                )
                                showDeleteDialog = true
                            }
                            canSwipeRight && offset.value > 0 &&
                                (offset.value > width * 0.3f || velocity > 1500f) -> {
                                onPrimaryAction()
                                offset.animateTo(targetValue = 0f, animationSpec = spring())
                            }
                            else -> offset.animateTo(targetValue = 0f, animationSpec = spring())
                        }
                    }
                },
            ),
    ) {
        if (abs(offset.value) > 1f && rowSize.height > 0) {
            val deleting = offset.value < 0
            val pillWidth = with(density) { (abs(offset.value).toDp() - 14.dp).coerceAtLeast(0.dp) }
            if (pillWidth > 0.dp) {
                Box(
                    modifier = Modifier
                        .align(if (deleting) Alignment.CenterEnd else Alignment.CenterStart)
                        .padding(start = if (deleting) 0.dp else 6.dp, end = if (deleting) 6.dp else 0.dp)
                        .size(width = pillWidth, height = with(density) { (rowSize.height.toDp() - 8.dp).coerceAtLeast(0.dp) })
                        .clip(RoundedCornerShape(50))
                        .background(if (deleting) Color(0xFFEF5350) else Color(0xFF2196F3)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (pillWidth >= 28.dp) {
                        Icon(
                            imageVector = if (deleting) Icons.Outlined.Delete else primaryActionIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier.offset { IntOffset(x = offset.value.roundToInt(), y = 0) }
                .clip(
                    when {
                        offset.value < -1f -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                        offset.value > 1f -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                        else -> restingShape
                    },
                )
                .background(
                    when {
                        abs(offset.value) <= 1f || backgroundColor != Color.Transparent -> backgroundColor
                        isConnected -> Color.White.copy(alpha = 0.08f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                ),
        ) { content() }
    }
}
