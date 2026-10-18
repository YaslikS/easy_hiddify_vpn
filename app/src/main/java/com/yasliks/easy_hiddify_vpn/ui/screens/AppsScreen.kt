package com.yasliks.easy_hiddify_vpn.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.ui.components.base.ScreenWrapper
import com.yasliks.easy_hiddify_vpn.vm.AppsViewModel
import com.yasliks.hiddify_library_lib.EasyHiddify
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AppsScreen(hiddify: EasyHiddify, appsViewModel: AppsViewModel) {
    val isConnected by hiddify.state.connected.collectAsState()
    val apps by appsViewModel.apps.collectAsState(initial = persistentListOf())
    val searchAppsStr by appsViewModel.searchAppsStr.collectAsState("")
    val isAllSelected by appsViewModel.isAllSelected.collectAsState(false)

    ScreenWrapper(
        title = stringResource(R.string.tunneling), showTopBar = true,
        actions = {
            IconButton(
                enabled = !isConnected,
                onClick = {
                    appsViewModel.changeAllStateApps(!isAllSelected)
                },
            ) {
                Icon(if (!isAllSelected) Icons.Default.Checklist else Icons.Default.ClearAll, null)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchAppsStr, onValueChange = { appsViewModel.changeSearchAppsStr(it) },
                placeholder = { Text("Search by name or package...") },
                modifier = Modifier.fillMaxWidth().padding(
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = 8.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                singleLine = true, shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchAppsStr.isNotEmpty()) IconButton(onClick = {
                        appsViewModel.changeSearchAppsStr(
                            ""
                        )
                    }) { Icon(Icons.Default.Close, null) }
                }
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(apps, key = { it.packageName }) { app ->
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = {
                            Text(
                                app.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        supportingContent = {
                            Text(
                                app.packageName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingContent = {
                            Image(
                                rememberAsyncImagePainter(app.iconPath),
                                null,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                            )
                        },
                        trailingContent = {
                            Switch(
                                enabled = !isConnected,
                                checked = app.isSelected,
                                onCheckedChange = { appsViewModel.changeStateApp(app) })
                        },
                        modifier = Modifier.clickable(enabled = !isConnected) {
                            appsViewModel.changeStateApp(
                                app
                            )
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}