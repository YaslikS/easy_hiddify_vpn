package com.yasliks.easy_hiddify_vpn.ui.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun ListItemRow(
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    trailingSpacing: Dp = 12.dp,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(
            horizontal = 12.dp,
            vertical = 10.dp,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(modifier = Modifier.width(12.dp))
        }
        Box(modifier = Modifier.weight(1f)) { content() }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(trailingSpacing))
            trailing()
        }
    }
}
