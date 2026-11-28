package com.yasliks.easy_hiddify_vpn.ui.screens

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yas.database.data.ServerDTO
import com.yasliks.easy_hiddify_vpn.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerBottomSheet(
    serverToEdit: ServerDTO? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, config: String) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var serverName by rememberSaveable(serverToEdit?.id) {
        mutableStateOf(serverToEdit?.name ?: "")
    }
    var serverConfig by rememberSaveable(serverToEdit?.id) {
        mutableStateOf(serverToEdit?.config ?: "")
    }
    val pasteConfig: () -> Unit = remember(clipboard, scope, context, serverToEdit?.id) {
        {
            scope.launch {
                val clipText = clipboard.getClipEntry()?.clipData?.getItemAt(0)?.coerceToText(context)
                if (!clipText.isNullOrBlank()) serverConfig = clipText.toString()
            }
        }
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
                                onClick = pasteConfig,
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
                            onClick = pasteConfig,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null
                            )
                        }
                    }
                )
            }

            DebugServerPresets { name, config ->
                if (serverName.isBlank()) serverName = name
                serverConfig = config
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
