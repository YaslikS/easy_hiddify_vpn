package com.yasliks.easy_hiddify_vpn.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

internal val ColorScheme.listPanelColor: Color
    get() = surfaceVariant.copy(alpha = 0.4f)

internal fun Modifier.listPanel(
    color: Color,
    shape: Shape = RoundedCornerShape(16.dp),
): Modifier = clip(shape = shape).background(color = color)

internal fun <T> LazyListScope.dividedItems(
    items: List<T>,
    key: (T) -> Any,
    itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
    itemsIndexed(
        items = items,
        key = { _, item -> key(item) },
    ) { index, item ->
        itemContent(item)
        if (index < items.lastIndex) BaseHorizontalDivider()
    }
}
