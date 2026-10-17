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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.yas.database.data.ServerDTO
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.ui.components.ServerItem
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

    val selectedApps by appsViewModel.selectedApps.collectAsState(
        initial = persistentListOf(),
    )

    val servers by serverViewModel.servers.collectAsState(initial = emptyList())
    val lastServerId by serverViewModel.lastServerId.collectAsState(initial = 0)

    val effectiveSelectedId = if (servers.any { it.id == lastServerId }) {
        lastServerId
    } else {
        servers.firstOrNull()?.id ?: 0
    }

    LaunchedEffect(isConnected, serviceActiveServerId) {
        activeServerId = if (!isConnected) {
            null
        } else {
            serviceActiveServerId ?: activeServerId ?: effectiveSelectedId
        }
    }

    BackHandler(enabled = isEditMode) {
        isEditMode = false
    }

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

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startHiddify()
        }
    }

    val checkVpnPermissionAndStart = {
        val intent = VpnService.prepare(context)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startHiddify()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        checkVpnPermissionAndStart()
    }

    val handleStartClick = { config: String, serverName: String, serverId: Int? ->
        pendingConfig = config
        pendingServerName = serverName
        pendingServerId = serverId

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                checkVpnPermissionAndStart()
            }
        } else {
            checkVpnPermissionAndStart()
        }
    }

    ScreenWrapper(
        showTopBar = false
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) Color(0xFFE53935) else Color(0xFF4CAF50))
                        .clickable {
                            if (isConnected) {
                                hiddify.stopVpn()
                                activeServerId = null
                            } else {
                                val targetServer = servers.find { it.id == effectiveSelectedId }

                                if (targetServer != null) {
                                    serverViewModel.setLastServerId(targetServer.id)
                                    handleStartClick(
                                        targetServer.config,
                                        targetServer.name,
                                        targetServer.id
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.list_servers_empty),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isConnected) "STOP" else "START",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isConnected) "CONNECTED" else "DISCONNECTED",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isConnected) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(
                            id = R.string.speed,
                            "⬇️ ${traffic.getDownlinkSpeed()} || ⬆️ ${traffic.getUplinkSpeed()}"
                        ),
                        fontSize = 12.sp
                    )
                    Text(
                        text = stringResource(
                            id = R.string.total,
                            "⬇️ ${traffic.getDownlinkTotal()} || ⬆️ ${traffic.getUplinkTotal()}"
                        ),
                        fontSize = 12.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditMode) stringResource(R.string.editing_servers) else stringResource(R.string.saved_ervers),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )

                if (isEditMode) {
                    Button(
                        onClick = { isEditMode = false },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(stringResource(R.string.done))
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            serverToEdit = null
                            showServerSheet = true
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.add))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(servers, key = { it.id }) { server ->
                    val isCurrentServerConnected = isConnected && (activeServerId == server.id)
                    val isSelected = server.id == effectiveSelectedId

                    ServerItem(
                        server = server,
                        isSelected = isSelected,
                        isCurrentConnected = isCurrentServerConnected,
                        isEditMode = isEditMode,
                        onConnectClick = {
                            serverViewModel.setLastServerId(server.id)
                            handleStartClick(server.config, server.name, server.id)
                        },
                        onEditClick = {
                            serverToEdit = server
                            showServerSheet = true
                        },
                        onCopyClick = {
                            clipboardManager.setText(AnnotatedString(server.config))
                            Toast.makeText(
                                context,
                                context.getString(R.string.config_copied),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onDeleteClick = {
                            if (isCurrentServerConnected) {
                                hiddify.stopVpn()
                                activeServerId = null
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.connection_broken),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            if (server.id == effectiveSelectedId) {
                                val nextServerId = servers.firstOrNull { it.id != server.id }?.id ?: 0
                                serverViewModel.setLastServerId(nextServerId)
                            }

                            serverViewModel.remove(server)
                            Toast.makeText(
                                context,
                                context.getString(R.string.server_deleted),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onLongClick = {
                            if (!isEditMode) {
                                isEditMode = true
                            }
                        }
                    )
                }
            }
        }

        if (showServerSheet) {
            ServerBottomSheet(
                serverToEdit = serverToEdit,
                onDismiss = {
                    showServerSheet = false
                    serverToEdit = null
                },
                onSave = { name, config ->
                    if (serverToEdit != null) {
                        serverViewModel.update(
                            serverToEdit!!.copy(name = name, config = config)
                        )
                    } else {
                        serverViewModel.insert(config = config, name = name)
                    }
                    showServerSheet = false
                    serverToEdit = null
                }
            )
        }
    }
}