package com.yasliks.easy_hiddify_vpn.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.yas.database.data.AppDTO
import com.yasliks.easy_hiddify_vpn.ui.components.common.ToggleListItem

@Composable
fun AppListItem(
    app: AppDTO,
    isConnected: Boolean,
    onAppClick: (AppDTO) -> Unit,
    modifier: Modifier = Modifier,
) {
    ToggleListItem(
        title = app.name,
        subtitle = app.packageName,
        checked = app.isSelected,
        onCheckedChange = { onAppClick(app) },
        modifier = modifier,
        enabled = !isConnected,
        subtitleMaxLines = 1,
        leading = {
            AsyncImage(
                model = app.iconPathNotNull.ifEmpty { null },
                contentDescription = null,
                modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)),
            )
        },
    )
}
