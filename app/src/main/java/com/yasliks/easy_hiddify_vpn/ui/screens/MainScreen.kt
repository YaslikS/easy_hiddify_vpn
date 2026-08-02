package com.yasliks.easy_hiddify_vpn.ui.screens

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yasliks.easy_hiddify_vpn.BuildConfig
import com.yasliks.easy_hiddify_vpn.vm.AppsViewModel
import com.yasliks.hiddify_library_lib.EasyHiddify
import com.yasliks.hiddify_library_lib.extension.formatTraffic
import kotlinx.collections.immutable.persistentListOf

@Composable
fun MainScreen(
    appsViewModel: AppsViewModel,
    hiddify: EasyHiddify,
) {
    val context = LocalContext.current

    var pendingConfig by remember { mutableStateOf<String?>(null) }

    val isConnected by hiddify.state.connected.collectAsState()
    val status by hiddify.state.status.collectAsState()
    val logs by hiddify.logger.logs.collectAsState()
    val selectedApps by appsViewModel.selectedApps.collectAsState(
        initial = persistentListOf(),
    )

    // Start VPN
    val startHiddify = {
        pendingConfig?.let {
            hiddify.startVpn(
                configStr = it,
                serverName = "Name",
                appsList = selectedApps,
                isEnabledApps = true,
            )
        }
    }

    // Launcher VPN
    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startHiddify()
        }
    }

    // Check VPN permission
    val checkVpnPermissionAndStart = {
        val intent = VpnService.prepare(context)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startHiddify()
        }
    }

    // Check notification permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        checkVpnPermissionAndStart()
    }

    // Main entry point
    val handleStartClick = { config: String ->
        pendingConfig = config

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                /* context = */ context,
                /* permission = */ Manifest.permission.POST_NOTIFICATIONS,
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


    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Hiddify Lib Test App",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(if (isConnected) Color.Green else Color.Red)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isConnected) "CONNECTED" else "DISCONNECTED")
        }

        status?.let {
            Text(
                "Down: ${it.downlinkTotal.formatTraffic()} | Up: ${it.uplinkTotal.formatTraffic()}",
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = appsViewModel.config,
            onValueChange = {
                appsViewModel.changeConfig(it)
            },
            label = { Text("Config (VLESS/SS/JSON)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    handleStartClick(appsViewModel.config)
                },
                enabled = !isConnected,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) { Text("START", fontSize = 10.sp) }

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    hiddify.stopVpn()
                },
                enabled = isConnected,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) { Text("STOP", fontSize = 10.sp) }

            if (BuildConfig.DEBUG) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        appsViewModel.changeConfig(BuildConfig.vless_serv)
                    },
                    enabled = !isConnected,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("VLESS", fontSize = 10.sp) }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        appsViewModel.changeConfig(BuildConfig.ss_serv)
                    },
                    enabled = !isConnected,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("SS", fontSize = 10.sp) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Logs:", style = MaterialTheme.typography.titleMedium)

        Surface(
            modifier = Modifier.fillMaxSize().weight(1f),
            color = Color.Black,
            shape = MaterialTheme.shapes.small
        ) {
            LazyColumn(contentPadding = PaddingValues(8.dp)) {
                items(logs) { entry ->
                    Text(
                        text = entry.message,
                        color = when (entry.level) {
                            4 -> Color.Red
                            3 -> Color.Yellow
                            else -> Color.LightGray
                        },
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}