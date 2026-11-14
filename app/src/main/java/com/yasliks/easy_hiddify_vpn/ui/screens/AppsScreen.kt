package com.yasliks.easy_hiddify_vpn.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yas.database.data.AppDTO
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.ui.components.AppListItem
import com.yasliks.easy_hiddify_vpn.ui.components.base.ScreenWrapper
import com.yasliks.easy_hiddify_vpn.ui.components.base.TopBar
import com.yasliks.easy_hiddify_vpn.ui.components.common.BaseHorizontalDivider
import com.yasliks.easy_hiddify_vpn.vm.AppsFilter
import com.yasliks.easy_hiddify_vpn.vm.AppsViewModel
import com.yasliks.hiddify_library_lib.EasyHiddify
import kotlinx.collections.immutable.persistentListOf

@Composable
fun AppsScreen(
    hiddify: EasyHiddify,
    appsViewModel: AppsViewModel,
) {
    val isConnected by hiddify.state.connected.collectAsStateWithLifecycle()
    val apps by appsViewModel.apps.collectAsStateWithLifecycle(initialValue = persistentListOf())
    val searchAppsStr by appsViewModel.searchAppsStr.collectAsStateWithLifecycle()
    val appsFilter by appsViewModel.appsFilter.collectAsStateWithLifecycle()
    val isAllSelected by appsViewModel.isAllSelected.collectAsStateWithLifecycle(initialValue = false)

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    ScreenWrapper(
        title = stringResource(R.string.tunneling),
        showTopBar = !isLandscape,
        actions = {
            AppsTopBarActions(
                isConnected = isConnected,
                isAllSelected = isAllSelected,
                appsFilter = appsFilter,
                onFilterChange = appsViewModel::changeAppsFilter,
                onToggleAll = { appsViewModel.changeAllStateApps(!isAllSelected) },
            )
        },
    ) { innerPadding ->
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(.4f)
                        .fillMaxHeight()
                ) {
                    TopBar(
                        title = stringResource(R.string.tunneling),
                        actions = {
                            AppsTopBarActions(
                                isConnected = isConnected,
                                isAllSelected = isAllSelected,
                                appsFilter = appsFilter,
                                onFilterChange = appsViewModel::changeAppsFilter,
                                onToggleAll = { appsViewModel.changeAllStateApps(!isAllSelected) },
                            )
                        },
                    )
                    AppsSearchField(
                        searchQuery = searchAppsStr,
                        onQueryChange = appsViewModel::changeSearchAppsStr,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                AppsList(
                    apps = apps,
                    isConnected = isConnected,
                    onAppClick = appsViewModel::changeStateApp,
                    modifier = Modifier
                        .weight(.6f)
                        .statusBarsPadding()
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                AppsSearchField(
                    searchQuery = searchAppsStr,
                    onQueryChange = appsViewModel::changeSearchAppsStr,
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                )
                AppsList(
                    apps = apps,
                    isConnected = isConnected,
                    onAppClick = appsViewModel::changeStateApp,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
    }
}

@Composable
private fun AppsTopBarActions(
    isConnected: Boolean,
    isAllSelected: Boolean,
    appsFilter: AppsFilter,
    onFilterChange: (AppsFilter) -> Unit,
    onToggleAll: () -> Unit,
) {
    var showFilterMenu by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { showFilterMenu = true }) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = if (appsFilter != AppsFilter.ALL) {
                    MaterialTheme.colorScheme.primary
                } else {
                    LocalContentColor.current
                },
            )
        }

        DropdownMenu(
            expanded = showFilterMenu,
            onDismissRequest = { showFilterMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_all)) },
                trailingIcon = {
                    if (appsFilter == AppsFilter.ALL) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                },
                onClick = {
                    onFilterChange(AppsFilter.ALL)
                    showFilterMenu = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_enabled)) },
                trailingIcon = {
                    if (appsFilter == AppsFilter.ENABLED) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                },
                onClick = {
                    onFilterChange(AppsFilter.ENABLED)
                    showFilterMenu = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filter_disabled)) },
                trailingIcon = {
                    if (appsFilter == AppsFilter.DISABLED) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    }
                },
                onClick = {
                    onFilterChange(AppsFilter.DISABLED)
                    showFilterMenu = false
                },
            )
        }
    }

    IconButton(
        enabled = !isConnected,
        onClick = onToggleAll,
    ) {
        Icon(
            imageVector = if (!isAllSelected) {
                Icons.Default.Checklist
            } else {
                Icons.Default.ClearAll
            },
            contentDescription = null,
        )
    }
}

@Composable
private fun AppsSearchField(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_apps_placeholder)) },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        leadingIcon = {
            Icon(Icons.Default.Search, null)
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, null)
                }
            }
        },
    )
}

@Composable
private fun AppsList(
    apps: List<AppDTO>,
    isConnected: Boolean,
    onAppClick: (AppDTO) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        contentPadding = PaddingValues(0.dp)
    ) {
        itemsIndexed(items = apps, key = { _, app -> app.packageName }) { index, app ->
            AppListItem(
                app = app,
                isConnected = isConnected,
                onAppClick = onAppClick
            )

            if (index < apps.lastIndex) {
                BaseHorizontalDivider()
            }
        }
    }
}