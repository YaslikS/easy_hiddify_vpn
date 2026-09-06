package com.yasliks.easy_hiddify_vpn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
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
    val clipboardManager = LocalClipboardManager.current
    var serverName by remember(serverToEdit) { mutableStateOf(serverToEdit?.name ?: "") }
    var serverConfig by remember(serverToEdit) { mutableStateOf(serverToEdit?.config ?: "") }

    val isSaveEnabled = serverName.isNotBlank() && serverConfig.isNotBlank()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (serverToEdit != null) stringResource(R.string.edit_server) else stringResource(R.string.add_server),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

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
                maxLines = 4,
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

            if (BuildConfig.DEBUG) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (serverName.isBlank()) serverName = "Debug VLESS"
                            serverConfig = BuildConfig.vless_serv
                        }
                    ) {
                        Text("VLESS")
                    }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (serverName.isBlank()) serverName = "Debug SS"
                            serverConfig = BuildConfig.ss_serv
                        }
                    ) {
                        Text("SS")
                    }

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (serverName.isBlank()) serverName = "Debug TROJAN"
                            serverConfig = BuildConfig.trojan
                        }
                    ) {
                        Text("TROJAN")
                    }
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
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