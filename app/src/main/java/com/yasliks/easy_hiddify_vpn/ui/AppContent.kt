package com.yasliks.easy_hiddify_vpn.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.extensions.SystemBarsManager
import com.yasliks.easy_hiddify_vpn.ui.components.base.AppRoute
import com.yasliks.easy_hiddify_vpn.ui.components.base.AppScaffold
import com.yasliks.easy_hiddify_vpn.ui.components.common.InfoDialog
import com.yasliks.easy_hiddify_vpn.ui.screens.AppsScreen
import com.yasliks.easy_hiddify_vpn.ui.screens.ImportSheet
import com.yasliks.easy_hiddify_vpn.ui.screens.LogScreen
import com.yasliks.easy_hiddify_vpn.ui.screens.MainScreen
import com.yasliks.easy_hiddify_vpn.ui.screens.SettingsScreen
import com.yasliks.easy_hiddify_vpn.ui.theme.EasyHiddifyVpnTheme
import com.yasliks.easy_hiddify_vpn.vm.AppsViewModel
import com.yasliks.easy_hiddify_vpn.vm.ImportViewModel
import com.yasliks.easy_hiddify_vpn.vm.MainViewModel
import com.yasliks.easy_hiddify_vpn.vm.ServerViewModel
import com.yasliks.easy_hiddify_vpn.vm.SettingsViewModel
import com.yasliks.hiddify_library_lib.EasyHiddify

@Composable
internal fun AppContent(
    hiddify: EasyHiddify,
    mainViewModel: MainViewModel,
    importViewModel: ImportViewModel,
    onDeleteSubscription: (String) -> Unit,
) {
    val isConnected by hiddify.state.connected.collectAsStateWithLifecycle()
    val currentRoute by mainViewModel.currentRoute.collectAsStateWithLifecycle()
    val showPermissionDialog by mainViewModel.showPermissionDialog.collectAsStateWithLifecycle()
    val showEmptyServerDialog by mainViewModel.showEmptyServerDialog.collectAsStateWithLifecycle()

    SystemBarsManager(
        isMainScreen = currentRoute == AppRoute.Main,
        isConnected = isConnected,
    )
    EasyHiddifyVpnTheme(isConnected = isConnected) {
        ImportSheet(viewModel = importViewModel)
        if (showPermissionDialog) {
            InfoDialog(
                title = stringResource(id = R.string.permission_dialog_title),
                message = stringResource(id = R.string.permission_dialog_desc),
                onDismiss = { mainViewModel.setShowPermissionDialog(show = false) },
            )
        }
        if (showEmptyServerDialog) {
            InfoDialog(
                title = stringResource(id = R.string.empty_server_dialog_title),
                message = stringResource(id = R.string.empty_server_dialog_desc),
                onDismiss = { mainViewModel.setShowEmptyServerDialog(show = false) },
            )
        }

        AppScaffold(
            currentRoute = currentRoute,
            isConnected = isConnected,
            onNavigate = mainViewModel::navigateTo,
        ) {
            when (currentRoute) {
                AppRoute.Main -> {
                    val disconnectText = stringResource(id = R.string.push_button_text)
                    MainScreen(
                        serverViewModel = viewModel<ServerViewModel>(),
                        hiddify = hiddify,
                        onImportClick = { importViewModel.open() },
                        onRefreshSubscription = importViewModel::refreshSubscription,
                        onDeleteSubscription = onDeleteSubscription,
                        onStartServer = { config, serverName, serverId ->
                            mainViewModel.startServer(
                                hiddify = hiddify,
                                config = config,
                                serverName = serverName,
                                serverId = serverId,
                                disconnectText = disconnectText,
                            )
                        },
                    )
                }
                AppRoute.Apps -> AppsScreen(
                    hiddify = hiddify,
                    appsViewModel = viewModel<AppsViewModel>(),
                )
                AppRoute.Settings -> SettingsScreen(
                    viewModel = viewModel<SettingsViewModel>(),
                    onOpenLogs = { mainViewModel.navigateTo(route = AppRoute.Logs) },
                )
                AppRoute.Logs -> {
                    val onBack: () -> Unit = remember(mainViewModel) {
                        { mainViewModel.navigateTo(route = AppRoute.Settings) }
                    }
                    BackHandler(onBack = onBack)
                    LogScreen(
                        hiddify = hiddify,
                        onBack = onBack,
                    )
                }
            }
        }
    }
}
