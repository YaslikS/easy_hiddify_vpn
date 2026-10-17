package com.yasliks.easy_hiddify_vpn.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.ui.components.SettingSwitchItem
import com.yasliks.easy_hiddify_vpn.ui.components.base.ScreenWrapper
import com.yasliks.hiddify_library_lib.EasyHiddify

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    hiddify: EasyHiddify,
    onOpenLogs: () -> Unit,
) {
    val dnsServer by hiddify.settings.dnsServer.collectAsState()
    val mtu by hiddify.settings.mtu.collectAsState()
    val enableIPv6 by hiddify.settings.enableIPv6.collectAsState()
    val bypassLan by hiddify.settings.bypassLan.collectAsState()
    val logLevel by hiddify.settings.logLevel.collectAsState()

    val strictRoute by hiddify.settings.strictRoute.collectAsState()
    val enableMux by hiddify.settings.enableMux.collectAsState()
    val enableLocalProxy by hiddify.settings.enableLocalProxy.collectAsState()
    val enableAdBlock by hiddify.settings.enableAdBlock.collectAsState()
    val customDirectDomains by hiddify.settings.customDirectDomains.collectAsState()
    val enableFakeIp by hiddify.settings.enableFakeIp.collectAsState()

    var dnsInput by remember { mutableStateOf(dnsServer) }
    var mtuInput by remember { mutableStateOf(mtu.toString()) }
    var domainsInput by remember { mutableStateOf(customDirectDomains) }

    ScreenWrapper(
        title = stringResource(R.string.settings),
        showTopBar = true
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenLogs)
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "View App Logs",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Text(
                    text = "Warning: Changes will take effect only after reconnecting the VPN.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                SettingSwitchItem(
                    title = "Strict Route",
                    subtitle = "Prevents IP leaks during network changes (e.g., Wi-Fi to Cellular).",
                    checked = strictRoute,
                    onCheckedChange = { hiddify.settings.setStrictRoute(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                SettingSwitchItem(
                    title = "Multiplexing (Mux)",
                    subtitle = "Combines multiple connections into one. Reduces latency and saves battery.",
                    checked = enableMux,
                    onCheckedChange = { hiddify.settings.setEnableMux(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                SettingSwitchItem(
                    title = "FakeIP (DNS Speedup)",
                    subtitle = "Instantly returns a fake IP for DNS queries, routing them securely through the proxy. Drastically improves browsing speed.",
                    checked = enableFakeIp,
                    onCheckedChange = { hiddify.settings.setEnableFakeIp(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                SettingSwitchItem(
                    title = "AdBlock (geosite)",
                    subtitle = "Blocks ads and trackers at the DNS level. Requires core support.",
                    checked = enableAdBlock,
                    onCheckedChange = { hiddify.settings.setEnableAdBlock(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                SettingSwitchItem(
                    title = "Allow LAN Connection",
                    subtitle = "Opens HTTP/SOCKS5 proxy on port 10808. Allows other devices on your Wi-Fi to use this VPN.",
                    checked = enableLocalProxy,
                    onCheckedChange = { hiddify.settings.setEnableLocalProxy(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                SettingSwitchItem(
                    title = "Enable IPv6",
                    subtitle = "Disable this if your ISP doesn't fully support IPv6 or to prevent potential IP leaks.",
                    checked = enableIPv6,
                    onCheckedChange = { hiddify.settings.setEnableIPv6(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            item {
                SettingSwitchItem(
                    title = "Bypass LAN",
                    subtitle = "Allows connection to local devices (printers, smart home) without VPN.",
                    checked = bypassLan,
                    onCheckedChange = { hiddify.settings.setBypassLan(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = domainsInput,
                    onValueChange = {
                        domainsInput = it
                        hiddify.settings.setCustomDirectDomains(it)
                    },
                    label = { Text("Split Tunneling Domains") },
                    supportingText = {
                        Text("Comma-separated domains to bypass the VPN (e.g., ru, sberbank.com).")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    )
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedTextField(
                    value = dnsInput,
                    onValueChange = {
                        dnsInput = it
                        hiddify.settings.setDnsServer(it)
                    },
                    label = { Text("Custom DNS") },
                    supportingText = {
                        Text("Overrides the default DNS server. Default: 8.8.8.8")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    )
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedTextField(
                    value = mtuInput,
                    onValueChange = {
                        mtuInput = it
                        it.toIntOrNull()?.let { validMtu ->
                            hiddify.settings.setMtu(validMtu)
                        }
                    },
                    label = { Text("MTU (Maximum Transmission Unit)") },
                    supportingText = {
                        Text("Defines packet size. Decrease if connection drops. Default: 1500")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                var expanded by remember { mutableStateOf(false) }
                val logLevels = listOf("trace", "debug", "info", "warn", "error", "fatal")

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = logLevel.uppercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Log Level") },
                        supportingText = {
                            Text("Use 'DEBUG' for troubleshooting, and 'WARN' or 'ERROR' to save battery.")
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        logLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level.uppercase()) },
                                onClick = {
                                    hiddify.settings.setLogLevel(level)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}