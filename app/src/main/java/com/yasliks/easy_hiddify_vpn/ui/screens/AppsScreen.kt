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
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.yasliks.easy_hiddify_vpn.vm.AppsViewModel
import com.yasliks.hiddify_library_lib.EasyHiddify
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AppsScreen(
    hiddify: EasyHiddify,
    appsViewModel: AppsViewModel,
) {
    val isConnected by hiddify.state.connected.collectAsState()
    val apps by appsViewModel.apps.collectAsState(
        initial = persistentListOf(),
    )
    val searchAppsStr by appsViewModel.searchAppsStr.collectAsState("")
    val isAllSelected by appsViewModel.isAllSelected.collectAsState(false)

    Column(
        modifier = Modifier
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Tunneling",
                style = MaterialTheme.typography.headlineMedium,
            )

            Row {
                if (!isAllSelected) {
                    Icon(
                        Icons.Default.Checklist,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                appsViewModel.changeAllStateApps()
                            }
                            .padding(10.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.ClearAll,
                        contentDescription = null,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                appsViewModel.changeAllStateApps(isSelected = false)
                            }
                            .padding(10.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchAppsStr,
            onValueChange = {
                appsViewModel.changeSearchAppsStr(it)
            },
            label = { Text("Name or Package") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp),
            maxLines = 4,
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
                                .padding(top = 6.dp, bottom = 6.dp, end = 6.dp),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            Text(text = app.packageName)
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