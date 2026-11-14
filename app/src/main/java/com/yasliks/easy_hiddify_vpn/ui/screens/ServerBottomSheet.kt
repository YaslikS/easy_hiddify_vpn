package com.yasliks.easy_hiddify_vpn.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yas.database.data.ServerDTO
import com.yasliks.easy_hiddify_vpn.BuildConfig
import com.yasliks.easy_hiddify_vpn.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerBottomSheet(
    serverToEdit: ServerDTO? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, config: String) -> Unit,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var serverName by rememberSaveable(serverToEdit?.id) {
        mutableStateOf(serverToEdit?.name ?: "")
    }
    var serverConfig by rememberSaveable(serverToEdit?.id) {
        mutableStateOf(serverToEdit?.config ?: "")
    }

    val isSaveEnabled = serverName.isNotBlank() && serverConfig.isNotBlank()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (serverToEdit != null) stringResource(R.string.edit_server) else stringResource(
                    R.string.add_server
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = serverName,
                        onValueChange = { serverName = it },
                        label = { Text(stringResource(R.string.server_name)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = serverConfig,
                        onValueChange = { serverConfig = it },
                        label = { Text(stringResource(R.string.config)) },
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    val clipText = clipboardManager.getText()?.text
                                    if (!clipText.isNullOrBlank()) {
                                        serverConfig = clipText
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                }
            } else {
                OutlinedTextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    label = { Text(stringResource(R.string.server_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = serverConfig,
                    onValueChange = { serverConfig = it },
                    label = { Text(stringResource(R.string.config)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val clipText = clipboardManager.getText()?.text
                                if (!clipText.isNullOrBlank()) {
                                    serverConfig = clipText
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null
                            )
                        }
                    }
                )
            }

            if (BuildConfig.DEBUG) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            @SuppressLint("LocalContextGetResourceValueCall")
                            if (serverName.isBlank()) {
                                serverName = context.getString(R.string.debug_vless_name)
                            }
                            serverConfig = BuildConfig.vless_serv
                        }
                    ) {
                        Text(stringResource(R.string.debug_vless))
                    }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            @SuppressLint("LocalContextGetResourceValueCall")
                            if (serverName.isBlank()) {
                                serverName = context.getString(R.string.debug_ss_name)
                            }
                            serverConfig = BuildConfig.ss_serv
                        }
                    ) {
                        Text(stringResource(R.string.debug_ss))
                    }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            @SuppressLint("LocalContextGetResourceValueCall")
                            if (serverName.isBlank()) {
                                serverName = context.getString(R.string.debug_trojan_name)
                            }
                            serverConfig = BuildConfig.trojan
                        }
                    ) {
                        Text(stringResource(R.string.debug_trojan))
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = isSaveEnabled,
                onClick = {
                    onSave(serverName.trim(), serverConfig.trim())
                }
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}