package com.yasliks.easy_hiddify_vpn

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.yasliks.easy_hiddify_vpn.extensions.SystemBarsManager
import com.yasliks.easy_hiddify_vpn.prefs.AppPrefs
import com.yasliks.easy_hiddify_vpn.ui.components.base.AppRoute
import com.yasliks.easy_hiddify_vpn.ui.components.base.BottomNavItem
import com.yasliks.easy_hiddify_vpn.ui.screens.AppsScreen
import com.yasliks.easy_hiddify_vpn.ui.screens.LogScreen
import com.yasliks.easy_hiddify_vpn.ui.screens.MainScreen
import com.yasliks.easy_hiddify_vpn.ui.screens.SettingsScreen
import com.yasliks.easy_hiddify_vpn.ui.theme.EasyHiddifyVpnTheme
import com.yasliks.easy_hiddify_vpn.vm.AppsViewModel
import com.yasliks.easy_hiddify_vpn.vm.MainViewModel
import com.yasliks.easy_hiddify_vpn.vm.ServerViewModel
import com.yasliks.easy_hiddify_vpn.vm.SettingsViewModel
import com.yasliks.hiddify_library_lib.EasyHiddify
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val hiddify get() = EasyHiddify.instance

    private val mainViewModel: MainViewModel by viewModels()
    private val appsViewModel: AppsViewModel by viewModels()
    private val serverViewModel: ServerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)
        if (intent?.action != AppPrefs.RECEIVER_QUICK_CONNECT) {
            mainViewModel.checkAutoStart(
                hiddify = hiddify,
                hasPermissions = hasRequiredPermissions(),
                disconnectText = getString(R.string.push_button_text),
            )
        }

        setContent {
            val isConnected by hiddify.state.connected.collectAsState()

            val currentRouteStr by mainViewModel.currentRoute.collectAsState()
            val showPermDialog by mainViewModel.showPermissionDialog.collectAsState()
            val showEmptyDialog by mainViewModel.showEmptyServerDialog.collectAsState()

            val currentPage = remember(currentRouteStr) {
                when (currentRouteStr) {
                    AppRoute.Apps.route -> AppRoute.Apps
                    AppRoute.Settings.route -> AppRoute.Settings
                    AppRoute.Logs.route -> AppRoute.Logs
                    else -> AppRoute.Main
                }
            }

            SystemBarsManager(
                isMainScreen = currentPage is AppRoute.Main,
                isConnected = isConnected,
            )

            EasyHiddifyVpnTheme(isConnected = isConnected) {
                if (showPermDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            mainViewModel.setShowPermissionDialog(false)
                        },
                        title = {
                            Text(text = stringResource(R.string.permission_dialog_title))
                        },
                        text = {
                            Text(text = stringResource(R.string.permission_dialog_desc))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = { mainViewModel.setShowPermissionDialog(false) }
                            ) {
                                Text(text = stringResource(R.string.ok))
                            }
                        }
                    )
                }

                if (showEmptyDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            mainViewModel.setShowEmptyServerDialog(false)
                        },
                        title = {
                            Text(text = stringResource(R.string.empty_server_dialog_title))
                        },
                        text = {
                            Text(text = stringResource(R.string.empty_server_dialog_desc))
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    mainViewModel.setShowEmptyServerDialog(false)
                                },
                            ) {
                                Text(text = stringResource(R.string.ok))
                            }
                        }
                    )
                }

                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                val bottomItems = listOf(
                    BottomNavItem.MainBottomItem,
                    BottomNavItem.ApplicationsBottomItem,
                    BottomNavItem.SettingsBottomItem,
                )

                val navBarContainerColor by animateColorAsState(
                    targetValue = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surface,
                    animationSpec = tween(1400),
                    label = "",
                )
                val indicatorColor by animateColorAsState(
                    targetValue = if (isConnected) Color.White else MaterialTheme.colorScheme.secondaryContainer,
                    animationSpec = tween(1400),
                    label = "",
                )
                val selectedIconColor by animateColorAsState(
                    targetValue = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSecondaryContainer,
                    animationSpec = tween(1400),
                    label = "",
                )
                val selectedTextColor by animateColorAsState(
                    targetValue = if (isConnected) Color.White else MaterialTheme.colorScheme.onSurface,
                    animationSpec = tween(1400),
                    label = "",
                )
                val unselectedColor by animateColorAsState(
                    targetValue = if (isConnected) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(1400),
                    label = "",
                )

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (!isLandscape) {
                            NavigationBar(containerColor = navBarContainerColor) {
                                bottomItems.forEach { screen ->
                                    NavigationBarItem(
                                        selected = currentPage.getActiveBottomTab() == screen.appRoute,
                                        onClick = {
                                            mainViewModel.navigateTo(screen.appRoute.route)
                                        },
                                        icon = {
                                            Icon(screen.icon, contentDescription = null)
                                        },
                                        label = {
                                            Text(text = stringResource(screen.stringId))
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = indicatorColor,
                                            selectedIconColor = selectedIconColor,
                                            selectedTextColor = selectedTextColor,
                                            unselectedIconColor = unselectedColor,
                                            unselectedTextColor = unselectedColor,
                                        )
                                    )
                                }
                            }
                        }
                    },
                ) { paddingValues ->
                    Row(modifier = Modifier.fillMaxSize()) {
                        if (isLandscape) {
                            Surface(color = navBarContainerColor) {
                                NavigationRail(
                                    containerColor = Color.Transparent,
                                    modifier = Modifier.windowInsetsPadding(
                                        insets = WindowInsets.systemBars.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)
                                    )
                                ) {
                                    bottomItems.forEach { screen ->
                                        NavigationRailItem(
                                            selected = currentPage.getActiveBottomTab() == screen.appRoute,
                                            onClick = {
                                                mainViewModel.navigateTo(screen.appRoute.route)
                                            },
                                            icon = {
                                                Icon(screen.icon, contentDescription = null)
                                            },
                                            label = {
                                                Text(text = stringResource(screen.stringId))
                                            },
                                            colors = NavigationRailItemDefaults.colors(
                                                indicatorColor = indicatorColor,
                                                selectedIconColor = selectedIconColor,
                                                selectedTextColor = selectedTextColor,
                                                unselectedIconColor = unselectedColor,
                                                unselectedTextColor = unselectedColor,
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = if (!isLandscape) paddingValues.calculateBottomPadding() else 0.dp)
                                .windowInsetsPadding(
                                    insets = if (isLandscape) {
                                        WindowInsets.systemBars.only(WindowInsetsSides.End)
                                    } else {
                                        WindowInsets(0.dp)
                                    }
                                )
                        ) {
                            when (currentPage) {
                                is AppRoute.Main -> MainScreen(
                                    serverViewModel = serverViewModel,
                                    hiddify = hiddify,
                                    onStartServer = { config, serverName, serverId ->
                                        mainViewModel.startServer(
                                            hiddify = hiddify,
                                            config = config,
                                            serverName = serverName,
                                            serverId = serverId,
                                            disconnectText = getString(R.string.push_button_text),
                                        )
                                    },
                                )

                                is AppRoute.Apps -> AppsScreen(hiddify, appsViewModel)
                                is AppRoute.Settings -> SettingsScreen(settingsViewModel) {
                                    mainViewModel.navigateTo(AppRoute.Logs.route)
                                }

                                is AppRoute.Logs -> {
                                    BackHandler {
                                        mainViewModel.navigateTo(AppRoute.Settings.route)
                                    }
                                    LogScreen(hiddify) {
                                        mainViewModel.navigateTo(AppRoute.Settings.route)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun hasRequiredPermissions(): Boolean {
        val hasVpnPermission = VpnService.prepare(this) == null
        val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                /* context = */ this,
                /* permission = */ Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else true
        return hasVpnPermission && hasNotificationPermission
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(AppPrefs.SHOW_PERMISSION_DIALOG, false) == true) {
            mainViewModel.setShowPermissionDialog(true)
            intent.removeExtra(AppPrefs.SHOW_PERMISSION_DIALOG)
        }

        if (intent?.getBooleanExtra(AppPrefs.EMPTY_SERVER_DIALOG, false) == true) {
            mainViewModel.setShowEmptyServerDialog(true)
            intent.removeExtra(AppPrefs.EMPTY_SERVER_DIALOG)
        }

        if (intent?.action == AppPrefs.RECEIVER_QUICK_CONNECT) {
            mainViewModel.handleQuickConnect(
                hiddify = hiddify,
                hasPermissions = hasRequiredPermissions(),
                disconnectText = getString(R.string.push_button_text),
            )
        }
    }
}