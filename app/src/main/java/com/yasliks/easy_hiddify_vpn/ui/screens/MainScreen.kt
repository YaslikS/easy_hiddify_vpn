package com.yasliks.easy_hiddify_vpn.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.VpnService
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yas.database.data.ServerDTO
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.ui.components.ServerItem
import com.yasliks.easy_hiddify_vpn.ui.components.base.ScreenWrapper
import com.yasliks.easy_hiddify_vpn.ui.components.common.BaseHorizontalDivider
import com.yasliks.easy_hiddify_vpn.ui.components.main.TrafficCard
import com.yasliks.easy_hiddify_vpn.ui.components.main.VpnButton
import com.yasliks.easy_hiddify_vpn.vm.AppsViewModel
import com.yasliks.easy_hiddify_vpn.vm.ServerViewModel
import com.yasliks.hiddify_library_lib.EasyHiddify
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.hypot

@Composable
fun MainScreen(
    appsViewModel: AppsViewModel,
    serverViewModel: ServerViewModel,
    hiddify: EasyHiddify,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var pendingConfig by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingServerName by rememberSaveable { mutableStateOf("") }
    var pendingServerId by rememberSaveable { mutableStateOf<Int?>(null) }
    var activeServerId by rememberSaveable { mutableStateOf<Int?>(null) }

    var showServerSheet by rememberSaveable { mutableStateOf(false) }
    var serverToEdit by remember { mutableStateOf<ServerDTO?>(null) }

    val isConnected by hiddify.state.connected.collectAsStateWithLifecycle()
    val serviceActiveServerId by hiddify.state.activeServerId.collectAsStateWithLifecycle()
    val selectedApps by appsViewModel.selectedApps.collectAsStateWithLifecycle(initialValue = persistentListOf())
    val servers by serverViewModel.servers.collectAsStateWithLifecycle(initialValue = emptyList())
    val lastServerId by serverViewModel.lastServerId.collectAsStateWithLifecycle(initialValue = 0)
    val isPinging by serverViewModel.isPinging.collectAsStateWithLifecycle()

    val effectiveSelectedId = remember(servers, lastServerId) {
        if (servers.any { it.id == lastServerId }) lastServerId else servers.firstOrNull()?.id ?: 0
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val density = LocalDensity.current

    val maxRadius = remember(configuration.screenWidthDp, configuration.screenHeightDp, density) {
        val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
        val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
        hypot(screenWidthPx, screenHeightPx)
    }

    var boxPositionInRoot by remember { mutableStateOf(Offset.Zero) }
    var buttonCenterInRoot by remember { mutableStateOf(Offset.Zero) }

    val animatedRadius by animateFloatAsState(
        targetValue = if (isConnected) maxRadius else 0f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "reveal"
    )

    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline

    val targetMainTextColor = remember(isConnected, onSurfaceColor) {
        if (isConnected) Color.White else onSurfaceColor
    }
    val targetSubTextColor = remember(isConnected, onSurfaceVariantColor) {
        if (isConnected) Color.White.copy(alpha = 0.8f) else onSurfaceVariantColor
    }
    val targetButtonContentColor = remember(isConnected, primaryColor) {
        if (isConnected) Color.White else primaryColor
    }
    val targetButtonBorderColor = remember(isConnected, outlineColor) {
        if (isConnected) Color.White.copy(alpha = 0.5f) else outlineColor
    }

    val mainTextColor by animateColorAsState(targetMainTextColor, tween(1000), label = "mainText")
    val subTextColor by animateColorAsState(targetSubTextColor, tween(1000), label = "subText")
    val buttonContentColor by animateColorAsState(
        targetButtonContentColor,
        tween(1000),
        label = "btnContent"
    )
    val buttonBorderColor by animateColorAsState(
        targetButtonBorderColor,
        tween(1000),
        label = "btnBorder"
    )

    LaunchedEffect(isConnected, serviceActiveServerId) {
        activeServerId = if (!isConnected) null else serviceActiveServerId ?: activeServerId
        ?: effectiveSelectedId
    }

    @SuppressLint("LocalContextGetResourceValueCall")
    val startHiddify = {
        pendingConfig?.let {
            hiddify.startVpn(
                configStr = it,
                serverName = pendingServerName,
                serverId = pendingServerId ?: 0,
                appsList = selectedApps,
                isEnabledApps = true,
                disconnectText = context.getString(R.string.push_button_text),
            )
            activeServerId = pendingServerId
        }
    }

    val vpnPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) startHiddify()
        }

    val checkVpnPermissionAndStart = {
        val intent = VpnService.prepare(context)
        if (intent != null) vpnPermissionLauncher.launch(intent) else startHiddify()
    }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            checkVpnPermissionAndStart()
        }

    val handleStartClick = { config: String, serverName: String, serverId: Int? ->
        pendingConfig = config
        pendingServerName = serverName
        pendingServerId = serverId
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else checkVpnPermissionAndStart()
        } else checkVpnPermissionAndStart()
    }

    val onToggleVpnClick = {
        if (isConnected) {
            hiddify.stopVpn()
            activeServerId = null
        } else {
            val targetServer = servers.find { it.id == effectiveSelectedId }
            if (targetServer != null) {
                serverViewModel.setLastServerId(targetServer.id)
                handleStartClick(targetServer.config, targetServer.name, targetServer.id)
            } else {
                @SuppressLint("LocalContextGetResourceValueCall")
                Toast.makeText(
                    context,
                    context.getString(R.string.list_servers_empty),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    ScreenWrapper(showTopBar = false) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    boxPositionInRoot = coordinates.positionInRoot()
                }
                .drawBehind {
                    val centerOffset = if (buttonCenterInRoot != Offset.Zero) {
                        buttonCenterInRoot - boxPositionInRoot
                    } else {
                        Offset(size.width / 2f, size.height / 2f)
                    }
                    drawCircle(Color(0xFF4CAF50), animatedRadius, centerOffset)
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(.4f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                        ) {
                            VpnControlSection(
                                isConnected = isConnected,
                                subTextColor = subTextColor,
                                onToggleClick = { onToggleVpnClick.invoke() },
                                onButtonPositioned = { buttonCenterInRoot = it }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TrafficSection(hiddify = hiddify, isConnected = isConnected)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Column(modifier = Modifier.weight(.6f).fillMaxHeight()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ServersHeaderSection(
                                hasServers = servers.isNotEmpty(),
                                isPinging = isPinging,
                                mainTextColor = mainTextColor,
                                buttonBorderColor = buttonBorderColor,
                                buttonContentColor = buttonContentColor,
                                onPingClick = serverViewModel::pingAllServers,
                                onAddServerClick = { serverToEdit = null; showServerSheet = true }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ServersListSection(
                                servers = servers,
                                effectiveSelectedId = effectiveSelectedId,
                                activeServerId = activeServerId,
                                isConnected = isConnected,
                                onConnectClick = { server ->
                                    serverViewModel.setLastServerId(server.id)
                                    handleStartClick(server.config, server.name, server.id)
                                },
                                onEditClick = { server ->
                                    serverToEdit = server; showServerSheet = true
                                },
                                onCopyClick = { server ->
                                    clipboardManager.setText(AnnotatedString(server.config))
                                    @SuppressLint("LocalContextGetResourceValueCall")
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.config_copied),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onDeleteClick = { server ->
                                    if (isConnected && activeServerId == server.id) {
                                        hiddify.stopVpn(); activeServerId = null
                                    }
                                    if (server.id == effectiveSelectedId) {
                                        serverViewModel.setLastServerId(
                                            servers.firstOrNull { it.id != server.id }?.id ?: 0
                                        )
                                    }
                                    serverViewModel.remove(server)
                                },
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        VpnControlSection(
                            isConnected = isConnected,
                            subTextColor = subTextColor,
                            onToggleClick = { onToggleVpnClick.invoke() },
                            onButtonPositioned = { buttonCenterInRoot = it }
                        )
                        TrafficSection(hiddify = hiddify, isConnected = isConnected)
                        Spacer(modifier = Modifier.height(12.dp))
                        ServersHeaderSection(
                            hasServers = servers.isNotEmpty(),
                            isPinging = isPinging,
                            mainTextColor = mainTextColor,
                            buttonBorderColor = buttonBorderColor,
                            buttonContentColor = buttonContentColor,
                            onPingClick = serverViewModel::pingAllServers,
                            onAddServerClick = { serverToEdit = null; showServerSheet = true }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ServersListSection(
                            servers = servers,
                            effectiveSelectedId = effectiveSelectedId,
                            activeServerId = activeServerId,
                            isConnected = isConnected,
                            onConnectClick = { server ->
                                serverViewModel.setLastServerId(server.id)
                                handleStartClick(server.config, server.name, server.id)
                            },
                            onEditClick = { server ->
                                serverToEdit = server; showServerSheet = true
                            },
                            onCopyClick = { server ->
                                clipboardManager.setText(AnnotatedString(server.config))
                                @SuppressLint("LocalContextGetResourceValueCall")
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.config_copied),
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onDeleteClick = { server ->
                                if (isConnected && activeServerId == server.id) {
                                    hiddify.stopVpn(); activeServerId = null
                                }
                                if (server.id == effectiveSelectedId) {
                                    serverViewModel.setLastServerId(
                                        servers.firstOrNull { it.id != server.id }?.id ?: 0
                                    )
                                }
                                serverViewModel.remove(server)
                            },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }

            if (showServerSheet) {
                ServerBottomSheet(
                    serverToEdit = serverToEdit,
                    onDismiss = { showServerSheet = false; serverToEdit = null },
                    onSave = { name, config ->
                        if (serverToEdit != null) {
                            serverViewModel.update(
                                serverToEdit!!.copy(
                                    name = name,
                                    config = config
                                )
                            )
                        } else {
                            serverViewModel.insert(config, name)
                        }
                        showServerSheet = false; serverToEdit = null
                    }
                )
            }
        }
    }
}

@Composable
private fun VpnControlSection(
    isConnected: Boolean,
    subTextColor: Color,
    onToggleClick: () -> Unit,
    onButtonPositioned: (Offset) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VpnButton(
            isConnected = isConnected,
            onClick = onToggleClick,
            modifier = Modifier.onGloballyPositioned { coordinates ->
                val pos = coordinates.positionInRoot()
                val size = coordinates.size
                onButtonPositioned(
                    Offset(
                        x = pos.x + size.width / 2f,
                        y = pos.y + size.height / 2f
                    )
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isConnected) stringResource(R.string.status_secure) else stringResource(R.string.status_ready),
            color = subTextColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun TrafficSection(
    hiddify: EasyHiddify,
    isConnected: Boolean
) {
    val traffic by hiddify.state.trafficStats.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TrafficCard(
            title = stringResource(R.string.download),
            speed = traffic.getDownlinkSpeed(),
            total = traffic.getDownlinkTotal(),
            icon = Icons.Rounded.ArrowDownward,
            iconTint = Color(0xFF4CAF50),
            isConnected = isConnected,
            modifier = Modifier.weight(1f)
        )
        TrafficCard(
            title = stringResource(R.string.upload),
            speed = traffic.getUplinkSpeed(),
            total = traffic.getUplinkTotal(),
            icon = Icons.Rounded.ArrowUpward,
            iconTint = Color(0xFF2196F3),
            isConnected = isConnected,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ServersHeaderSection(
    hasServers: Boolean,
    isPinging: Boolean,
    mainTextColor: Color,
    buttonBorderColor: Color,
    buttonContentColor: Color,
    onPingClick: () -> Unit,
    onAddServerClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.saved_ervers),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = mainTextColor
            )

            if (hasServers) {
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onPingClick,
                    enabled = !isPinging,
                    modifier = Modifier.size(28.dp)
                ) {
                    if (isPinging) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = mainTextColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = stringResource(R.string.cd_ping_servers),
                            tint = mainTextColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onAddServerClick,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp),
            border = BorderStroke(1.dp, buttonBorderColor),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = buttonContentColor)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.add), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ServersListSection(
    servers: List<ServerDTO>,
    effectiveSelectedId: Int,
    activeServerId: Int?,
    isConnected: Boolean,
    onConnectClick: (ServerDTO) -> Unit,
    onEditClick: (ServerDTO) -> Unit,
    onCopyClick: (ServerDTO) -> Unit,
    onDeleteClick: (ServerDTO) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isConnected) Color.White.copy(alpha = 0.05f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
        contentPadding = PaddingValues(0.dp)
    ) {
        itemsIndexed(
            items = servers,
            key = { _, server -> server.id },
        ) { index, server ->
            ServerItem(
                server = server,
                isSelected = server.id == effectiveSelectedId,
                isCurrentConnected = isConnected && activeServerId == server.id,
                isVpnActive = isConnected,
                onConnectClick = { onConnectClick(server) },
                onEditClick = { onEditClick(server) },
                onCopyClick = { onCopyClick(server) },
                onDeleteClick = { onDeleteClick(server) },
            )

            if (index < servers.lastIndex) {
                BaseHorizontalDivider()
            }
        }
    }
}