package com.yasliks.easy_hiddify_vpn.ui.components.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yasliks.easy_hiddify_vpn.R

@Composable
internal fun VpnControlPanel(
    isConnected: Boolean,
    serverName: String?,
    onToggle: () -> Unit,
    onButtonPositioned: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val containerColor by animateColorAsState(
        targetValue = if (isConnected) Color.Black.copy(alpha = 0.2f) else colors.surfaceContainer,
        animationSpec = tween(durationMillis = 500),
        label = "vpnControlContainer",
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        border = vpnPanelBorder(isConnected = isConnected),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        ) {
            VpnControlLayout(
                compact = maxWidth < 248.dp,
                details = { compact ->
                    VpnConnectionDetails(
                        isConnected = isConnected,
                        serverName = serverName,
                        centered = compact,
                    )
                },
                control = {
                    VpnButton(
                        isConnected = isConnected,
                        onClick = onToggle,
                        modifier = Modifier.onGloballyPositioned { coordinates ->
                            onButtonPositioned(
                                coordinates.positionInRoot() + Offset(
                                    x = coordinates.size.width / 2f,
                                    y = coordinates.size.height / 2f,
                                ),
                            )
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun VpnControlLayout(
    compact: Boolean,
    details: @Composable (Boolean) -> Unit,
    control: @Composable () -> Unit,
) {
    if (compact) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            control()
            details(true)
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) { details(false) }
            control()
        }
    }
}

@Composable
private fun VpnConnectionDetails(
    isConnected: Boolean,
    serverName: String?,
    centered: Boolean,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        Row(
            modifier = Modifier.background(
                color = if (isConnected) Color.Black.copy(alpha = 0.2f) else colors.surfaceContainerHighest,
                shape = RoundedCornerShape(12.dp),
            ).padding(
                horizontal = 8.dp,
                vertical = 5.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(6.dp).background(
                    color = if (isConnected) Color.White else colors.primary,
                    shape = CircleShape,
                ),
            )
            Text(
                text = stringResource(id = if (isConnected) R.string.status_secure else R.string.status_ready),
                style = MaterialTheme.typography.labelSmall,
                color = if (isConnected) Color.White else colors.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(weight = 1f, fill = false),
            )
        }
        if (serverName != null) {
            Text(
                text = serverName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isConnected) Color.White else colors.onSurface,
                textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
