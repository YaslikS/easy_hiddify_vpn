package com.yasliks.easy_hiddify_vpn.ui.screens

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.ui.components.ItemsGroup
import com.yasliks.easy_hiddify_vpn.ui.components.SettingSwitchItem
import com.yasliks.easy_hiddify_vpn.ui.components.base.ScreenWrapper
import com.yasliks.easy_hiddify_vpn.ui.components.base.TopBar
import com.yasliks.easy_hiddify_vpn.ui.components.common.BaseHorizontalDivider
import com.yasliks.easy_hiddify_vpn.vm.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenLogs: () -> Unit,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    ScreenWrapper(
        title = stringResource(R.string.settings),
        showTopBar = !isLandscape,
    ) { innerPadding ->
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(.3f).fillMaxHeight()) {
                    TopBar(title = stringResource(R.string.settings))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.settings_reconnect_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                SettingsListContent(
                    viewModel = viewModel,
                    onOpenLogs = onOpenLogs,
                    isLandscapeMode = true,
                    modifier = Modifier
                        .weight(.7f)
                        .fillMaxHeight()
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                )
            }
        } else {
            SettingsListContent(
                viewModel = viewModel,
                onOpenLogs = onOpenLogs,
                isLandscapeMode = false,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
                contentPadding = PaddingValues(bottom = 16.dp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SettingsListContent(
    viewModel: SettingsViewModel,
    onOpenLogs: () -> Unit,
    isLandscapeMode: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val hSettings = viewModel.hiddifySettings
    val autoStartVpn by viewModel.autoStartVpn.collectAsStateWithLifecycle(initialValue = false)
    val dnsServer by hSettings.dnsServer.collectAsStateWithLifecycle()
    val mtu by hSettings.mtu.collectAsStateWithLifecycle()
    val enableIPv6 by hSettings.enableIPv6.collectAsStateWithLifecycle()
    val bypassLan by hSettings.bypassLan.collectAsStateWithLifecycle()
    val logLevel by hSettings.logLevel.collectAsStateWithLifecycle()
    val strictRoute by hSettings.strictRoute.collectAsStateWithLifecycle()
    val enableMux by hSettings.enableMux.collectAsStateWithLifecycle()
    val enableLocalProxy by hSettings.enableLocalProxy.collectAsStateWithLifecycle()
    val enableAdBlock by hSettings.enableAdBlock.collectAsStateWithLifecycle()
    val customDirectDomains by hSettings.customDirectDomains.collectAsStateWithLifecycle()
    val enableFakeIp by hSettings.enableFakeIp.collectAsStateWithLifecycle()

    var dnsInput by remember(dnsServer) { mutableStateOf(dnsServer) }
    var mtuInput by remember(mtu) { mutableStateOf(mtu.toString()) }
    var domainsInput by remember(customDirectDomains) { mutableStateOf(customDirectDomains) }

    LazyColumn(modifier = modifier, contentPadding = contentPadding) {
        if (!isLandscapeMode) {
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = 8.dp, bottom = 6.dp, start = 24.dp, end = 24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_reconnect_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp, top = if (isLandscapeMode) 12.dp else 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onOpenLogs)
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.settings_view_logs),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        item {
            ItemsGroup(title = stringResource(R.string.settings_group_app)) {
                SettingSwitchItem(
                    title = stringResource(R.string.settings_auto_start_title),
                    subtitle = stringResource(R.string.settings_auto_start_subtitle),
                    checked = autoStartVpn,
                    onCheckedChange = viewModel::setAutoStartVpn,
                )
            }
        }

        item {
            ItemsGroup(title = stringResource(R.string.settings_group_performance)) {
                SettingSwitchItem(
                    title = stringResource(R.string.settings_fake_ip_title),
                    subtitle = stringResource(R.string.settings_fake_ip_subtitle),
                    checked = enableFakeIp,
                    onCheckedChange = viewModel::setEnableFakeIp,
                )
                BaseHorizontalDivider()
                SettingSwitchItem(
                    title = stringResource(R.string.settings_mux_title),
                    subtitle = stringResource(R.string.settings_mux_subtitle),
                    checked = enableMux,
                    onCheckedChange = viewModel::setEnableMux,
                )
                BaseHorizontalDivider()
                SettingSwitchItem(
                    title = stringResource(R.string.settings_ipv6_title),
                    subtitle = stringResource(R.string.settings_ipv6_subtitle),
                    checked = enableIPv6,
                    onCheckedChange = viewModel::setEnableIPv6,
                )
            }
        }

        item {
            ItemsGroup(title = stringResource(R.string.settings_group_routing)) {
                SettingSwitchItem(
                    title = stringResource(R.string.settings_strict_route_title),
                    subtitle = stringResource(R.string.settings_strict_route_subtitle),
                    checked = strictRoute,
                    onCheckedChange = viewModel::setStrictRoute,
                )
                BaseHorizontalDivider()
                SettingSwitchItem(
                    title = stringResource(R.string.settings_adblock_title),
                    subtitle = stringResource(R.string.settings_adblock_subtitle),
                    checked = enableAdBlock,
                    onCheckedChange = viewModel::setEnableAdBlock,
                )
                BaseHorizontalDivider()
                SettingSwitchItem(
                    title = stringResource(R.string.settings_bypass_lan_title),
                    subtitle = stringResource(R.string.settings_bypass_lan_subtitle),
                    checked = bypassLan,
                    onCheckedChange = viewModel::setBypassLan,
                )
                BaseHorizontalDivider()
                SettingSwitchItem(
                    title = stringResource(R.string.settings_allow_lan_title),
                    subtitle = stringResource(R.string.settings_allow_lan_subtitle),
                    checked = enableLocalProxy,
                    onCheckedChange = viewModel::setEnableLocalProxy,
                )
            }
        }

        item {
            AdvancedTuningGroup(
                domainsInput = domainsInput,
                onDomainsChange = { domainsInput = it; viewModel.setCustomDirectDomains(it) },
                dnsInput = dnsInput,
                onDnsChange = { dnsInput = it; viewModel.setDnsServer(it) },
                mtuInput = mtuInput,
                onMtuChange = {
                    mtuInput = it; it.toIntOrNull()?.let { validMtu -> viewModel.setMtu(validMtu) }
                },
                logLevel = logLevel,
                onLogLevelChange = viewModel::setLogLevel,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedTuningGroup(
    domainsInput: String, onDomainsChange: (String) -> Unit,
    dnsInput: String, onDnsChange: (String) -> Unit,
    mtuInput: String, onMtuChange: (String) -> Unit,
    logLevel: String, onLogLevelChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ItemsGroup(title = stringResource(R.string.settings_group_advanced)) {
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = domainsInput,
            onValueChange = onDomainsChange,
            label = { Text(stringResource(R.string.settings_split_domains_label)) },
            supportingText = { Text(stringResource(R.string.settings_split_domains_hint)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = dnsInput,
            onValueChange = onDnsChange,
            label = { Text(stringResource(R.string.settings_custom_dns_label)) },
            supportingText = { Text(stringResource(R.string.settings_custom_dns_hint)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = mtuInput,
            onValueChange = onMtuChange,
            label = { Text(stringResource(R.string.settings_mtu_label)) },
            supportingText = { Text(stringResource(R.string.settings_mtu_hint)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = logLevel.uppercase(),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.settings_log_level_label)) },
                supportingText = { Text(stringResource(R.string.settings_log_level_hint)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth().padding(horizontal = 12.dp)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                SettingsViewModel.logLevels.forEach { level ->
                    DropdownMenuItem(
                        text = { Text(level.uppercase()) },
                        onClick = { onLogLevelChange(level); expanded = false })
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}