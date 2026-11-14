package com.yasliks.easy_hiddify_vpn.ui.components.common

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BaseHorizontalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.background,
    thickness: Float = 1.5f,
) {
    HorizontalDivider(
        modifier = modifier,
        color = color,
        thickness = thickness.dp,
    )
}