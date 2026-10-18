package com.yasliks.easy_hiddify_vpn.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.ui.components.SettingSwitchItem
import com.yasliks.easy_hiddify_vpn.ui.components.base.ScreenWrapper
import com.yasliks.hiddify_library_lib.EasyHiddify

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(hiddify: EasyHiddify, onOpenLogs: () -> Unit) {
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

    ScreenWrapper(title = stringResource(R.string.settings), showTopBar = true) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 16.dp, bottom = 8.dp, start = 24.dp, end = 24.dp)
                ) {
                    Text(
                        text = "Warning: Changes take effect after reconnecting.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp, top = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onOpenLogs() }
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View App Logs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                SettingsGroup(title = "Performance & Network") {
                    SettingSwitchItem("FakeIP (DNS Speedup)", "Instantly returns a fake IP for DNS.", enableFakeIp) { hiddify.settings.setEnableFakeIp(it) }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingSwitchItem("Multiplexing (Mux)", "Reduces latency and saves battery.", enableMux) { hiddify.settings.setEnableMux(it) }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingSwitchItem("Enable IPv6", "Disable to prevent potential IP leaks.", enableIPv6) { hiddify.settings.setEnableIPv6(it) }
                }
            }

            item {
                SettingsGroup(title = "Routing & Security") {
                    SettingSwitchItem("Strict Route", "Prevents IP leaks during network changes.", strictRoute) { hiddify.settings.setStrictRoute(it) }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingSwitchItem("AdBlock (geosite)", "Blocks ads and trackers at the DNS level.", enableAdBlock) { hiddify.settings.setEnableAdBlock(it) }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingSwitchItem("Bypass LAN", "Access local network devices without VPN.", bypassLan) { hiddify.settings.setBypassLan(it) }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    SettingSwitchItem("Allow LAN Connection", "Share this VPN via proxy on port 10808.", enableLocalProxy) { hiddify.settings.setEnableLocalProxy(it) }
                }
            }

            item {
                SettingsGroup(title = "Advanced Tuning") {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = domainsInput, onValueChange = { domainsInput = it; hiddify.settings.setCustomDirectDomains(it) },
                        label = { Text("Split Tunneling Domains") }, supportingText = { Text("e.g., sberbank.com") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = dnsInput, onValueChange = { dnsInput = it; hiddify.settings.setDnsServer(it) },
                        label = { Text("Custom DNS") }, supportingText = { Text("Default: 8.8.8.8") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = mtuInput, onValueChange = { mtuInput = it; it.toIntOrNull()?.let { validMtu -> hiddify.settings.setMtu(validMtu) } },
                        label = { Text("MTU") }, supportingText = { Text("Decrease if connection drops. Default: 1500") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    var expanded by remember { mutableStateOf(false) }
                    val logLevels = listOf("trace", "debug", "info", "warn", "error", "fatal")
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        OutlinedTextField(
                            value = logLevel.uppercase(), onValueChange = {}, readOnly = true, label = { Text("Log Level") },
                            supportingText = { Text("Use 'DEBUG' for troubleshooting.") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(), modifier = Modifier.menuAnchor().fillMaxWidth().padding(horizontal = 16.dp)
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            logLevels.forEach { level -> DropdownMenuItem(text = { Text(level.uppercase()) }, onClick = { hiddify.settings.setLogLevel(level); expanded = false }) }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}