package com.yasliks.easy_hiddify_vpn.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import com.yas.database.data.ServerDTO
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.ui.components.ServerItem
import com.yasliks.easy_hiddify_vpn.ui.components.TrafficCard
import com.yasliks.easy_hiddify_vpn.ui.components.VpnButton
import com.yasliks.easy_hiddify_vpn.ui.components.base.ScreenWrapper
import com.yasliks.easy_hiddify_vpn.vm.AppsViewModel
import com.yasliks.easy_hiddify_vpn.vm.ServerViewModel
import com.yasliks.hiddify_library_lib.EasyHiddify
import kotlinx.collections.immutable.persistentListOf

@Composable
fun MainScreen(
    appsViewModel: AppsViewModel,
    serverViewModel: ServerViewModel,
    hiddify: EasyHiddify,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var pendingConfig by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingServerName by rememberSaveable { mutableStateOf("Name") }
    var pendingServerId by rememberSaveable { mutableStateOf<Int?>(null) }
    var activeServerId by rememberSaveable { mutableStateOf<Int?>(null) }

    var isEditMode by remember { mutableStateOf(false) }
    var showServerSheet by remember { mutableStateOf(false) }
    var serverToEdit by remember { mutableStateOf<ServerDTO?>(null) }

    val traffic by EasyHiddify.instance.state.trafficStats.collectAsState()
    val isConnected by hiddify.state.connected.collectAsState()
    val serviceActiveServerId by hiddify.state.activeServerId.collectAsState()

    val selectedApps by appsViewModel.selectedApps.collectAsState(initial = persistentListOf())
    val servers by serverViewModel.servers.collectAsState(initial = emptyList())
    val lastServerId by serverViewModel.lastServerId.collectAsState(initial = 0)
    val effectiveSelectedId = if (servers.any { it.id == lastServerId }) lastServerId else servers.firstOrNull()?.id ?: 0

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val maxRadius = kotlin.math.hypot(screenWidthPx, screenHeightPx)

    val statusBars = WindowInsets.statusBars
    val statusBarHeightPx = statusBars.getTop(density).toFloat()

    val animatedRadius by animateFloatAsState(
        targetValue = if (isConnected) maxRadius else 0f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "reveal"
    )

    val mainTextColor by animateColorAsState(if (isConnected) Color.White else MaterialTheme.colorScheme.onSurface, tween(1000), label = "")
    val subTextColor by animateColorAsState(if (isConnected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant, tween(1000), label = "")

    val buttonContentColor by animateColorAsState(if (isConnected) Color.White else MaterialTheme.colorScheme.primary, tween(1000), label = "")
    val buttonBorderColor by animateColorAsState(if (isConnected) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline, tween(1000), label = "")
    val buttonContainerColor by animateColorAsState(if (isConnected) Color.White else MaterialTheme.colorScheme.primary, tween(1000), label = "")
    val buttonOnContainerColor by animateColorAsState(if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onPrimary, tween(1000), label = "")

    LaunchedEffect(isConnected, serviceActiveServerId) {
        activeServerId = if (!isConnected) null else serviceActiveServerId ?: activeServerId ?: effectiveSelectedId
    }

    BackHandler(enabled = isEditMode) { isEditMode = false }

    val startHiddify = {
        pendingConfig?.let {
            hiddify.startVpn(
                configStr = it,
                serverName = pendingServerName,
                serverId = pendingServerId ?: 0,
                appsList = selectedApps,
                isEnabledApps = true,
            )
            activeServerId = pendingServerId
        }
    }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) startHiddify()
    }

    val checkVpnPermissionAndStart = {
        val intent = VpnService.prepare(context)
        if (intent != null) vpnPermissionLauncher.launch(intent) else startHiddify()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { checkVpnPermissionAndStart() }

    val handleStartClick = { config: String, serverName: String, serverId: Int? ->
        pendingConfig = config
        pendingServerName = serverName
        pendingServerId = serverId
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else checkVpnPermissionAndStart()
        } else checkVpnPermissionAndStart()
    }

    ScreenWrapper(showTopBar = false) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val centerY = statusBarHeightPx + 106.dp.toPx()
                    drawCircle(Color(0xFF4CAF50), animatedRadius, Offset(size.width / 2f, centerY))
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    VpnButton(
                        isConnected = isConnected,
                        onClick = {
                            if (isConnected) {
                                hiddify.stopVpn()
                                activeServerId = null
                            } else {
                                val targetServer = servers.find { it.id == effectiveSelectedId }
                                if (targetServer != null) {
                                    serverViewModel.setLastServerId(targetServer.id)
                                    handleStartClick(targetServer.config, targetServer.name, targetServer.id)
                                } else {
                                    Toast.makeText(context, context.getString(R.string.list_servers_empty), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isConnected) "CONNECTED" else "TAP TO CONNECT",
                        color = subTextColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        letterSpacing = 1.sp
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TrafficCard(
                        title = "Download", speed = traffic.getDownlinkSpeed(), total = traffic.getDownlinkTotal(),
                        icon = Icons.Rounded.ArrowDownward, iconTint = Color(0xFF4CAF50), isConnected = isConnected, modifier = Modifier.weight(1f)
                    )
                    TrafficCard(
                        title = "Upload", speed = traffic.getUplinkSpeed(), total = traffic.getUplinkTotal(),
                        icon = Icons.Rounded.ArrowUpward, iconTint = Color(0xFF2196F3), isConnected = isConnected, modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isEditMode) stringResource(R.string.editing_servers) else stringResource(R.string.saved_ervers),
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = mainTextColor
                    )
                    if (isEditMode) {
                        Button(
                            onClick = { isEditMode = false },
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonContainerColor,
                                contentColor = buttonOnContainerColor
                            )
                        ) {
                            Text(stringResource(R.string.done))
                        }
                    } else {
                        OutlinedButton(
                            onClick = { serverToEdit = null; showServerSheet = true },
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            border = BorderStroke(1.dp, buttonBorderColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = buttonContentColor)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.add))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(servers, key = { it.id }) { server ->
                        ServerItem(
                            server = server, isSelected = server.id == effectiveSelectedId, isCurrentConnected = isConnected && activeServerId == server.id,
                            isVpnActive = isConnected, isEditMode = isEditMode,
                            onConnectClick = { serverViewModel.setLastServerId(server.id); handleStartClick(server.config, server.name, server.id) },
                            onEditClick = { serverToEdit = server; showServerSheet = true },
                            onCopyClick = { clipboardManager.setText(AnnotatedString(server.config)); Toast.makeText(context, context.getString(R.string.config_copied), Toast.LENGTH_SHORT).show() },
                            onDeleteClick = {
                                if (isConnected && activeServerId == server.id) { hiddify.stopVpn(); activeServerId = null }
                                if (server.id == effectiveSelectedId) serverViewModel.setLastServerId(servers.firstOrNull { it.id != server.id }?.id ?: 0)
                                serverViewModel.remove(server)
                            },
                            onLongClick = { if (!isEditMode) isEditMode = true }
                        )
                    }
                }
            }

            if (showServerSheet) {
                ServerBottomSheet(
                    serverToEdit = serverToEdit,
                    onDismiss = { showServerSheet = false; serverToEdit = null },
                    onSave = { name, config ->
                        if (serverToEdit != null) serverViewModel.update(serverToEdit!!.copy(name = name, config = config)) else serverViewModel.insert(config, name)
                        showServerSheet = false; serverToEdit = null
                    }
                )
            }
        }
    }
}