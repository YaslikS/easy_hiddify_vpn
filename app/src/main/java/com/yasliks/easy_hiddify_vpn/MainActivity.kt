package com.yasliks.easy_hiddify_vpn

import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yasliks.hiddify_library_lib.EasyHiddify

class MainActivity : ComponentActivity() {

    private val hiddify get() = EasyHiddify.instance

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().systemBarsPadding(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    VpnScreen(
                        onStart = { config -> prepareAndStartVpn(config) },
                        onStop = { hiddify.stopVpn() }
                    )
                }
            }
        }
    }

    /**
     * Проверка разрешений системы перед запуском VPN
     */
    private fun prepareAndStartVpn(config: String) {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            pendingConfig = config
            vpnPermissionLauncher.launch(intent)
        } else {
            hiddify.startVpn(config, "Hiddify Test Server")
        }
    }

    private var pendingConfig: String? = null

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            pendingConfig?.let {
                hiddify.startVpn(it, "Hiddify Test Server")
            }
        }
    }

    @Composable
    fun VpnScreen(onStart: (String) -> Unit, onStop: () -> Unit) {
        var configText by remember {
            mutableStateOf("")
        }

        val isConnected by hiddify.state.connected.collectAsStateWithLifecycle()
        val status by hiddify.state.status.collectAsStateWithLifecycle()
        val logs by hiddify.logger.logs.collectAsStateWithLifecycle()

        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
            Text("Hiddify SDK Test", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
                    "Down: ${formatTraffic(it.downlinkTotal)} | Up: ${formatTraffic(it.uplinkTotal)}",
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = configText,
                onValueChange = { configText = it },
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
                    onClick = { onStart(configText) },
                    enabled = !isConnected,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("START", fontSize = 10.sp) }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onStop() },
                    enabled = isConnected,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("STOP", fontSize = 10.sp) }
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

    /**
     * Auxiliary function for formatting bytes in a readable form
     */
    private fun formatTraffic(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return if (mb > 1) "%.2f MB".format(mb) else "%.2f KB".format(kb)
    }
}