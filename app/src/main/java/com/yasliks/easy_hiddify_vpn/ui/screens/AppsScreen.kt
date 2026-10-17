package com.yasliks.easy_hiddify_vpn.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.ui.components.base.ScreenWrapper
import com.yasliks.easy_hiddify_vpn.vm.AppsViewModel
import com.yasliks.hiddify_library_lib.EasyHiddify
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AppsScreen(
    hiddify: EasyHiddify,
    appsViewModel: AppsViewModel,
) {
    val isConnected by hiddify.state.connected.collectAsState()
    val apps by appsViewModel.apps.collectAsState(initial = persistentListOf())
    val searchAppsStr by appsViewModel.searchAppsStr.collectAsState("")
    val isAllSelected by appsViewModel.isAllSelected.collectAsState(false)

    ScreenWrapper(
        title = stringResource(R.string.tunneling),
        showTopBar = true,
        actions = {
            if (!isAllSelected) {
                IconButton(onClick = { appsViewModel.changeAllStateApps() }) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = null
                    )
                }
            } else {
                IconButton(onClick = { appsViewModel.changeAllStateApps(isSelected = false) }) {
                    Icon(
                        imageVector = Icons.Default.ClearAll,
                        contentDescription = null
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchAppsStr,
                onValueChange = {
                    appsViewModel.changeSearchAppsStr(it)
                },
                label = { Text("Name or Package") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                trailingIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(24.dp)
                            .clickable {
                                appsViewModel.changeSearchAppsStr("")
                            },
                    )
                },
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(apps) { app ->
                    Column(
                        modifier = Modifier.clickable(
                            enabled = !isConnected
                        ) {
                            appsViewModel.changeStateApp(app)
                        },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = app.iconPath),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(vertical = 6.dp, horizontal = 6.dp),
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                enabled = !isConnected,
                                checked = app.isSelected,
                                onCheckedChange = {
                                    appsViewModel.changeStateApp(app)
                                },
                            )
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}