package com.yasliks.easy_hiddify_vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yasliks.easy_hiddify_vpn.ui.components.base.AppRoute
import com.yasliks.easy_hiddify_vpn.ui.components.base.BottomNavItem
import com.yasliks.easy_hiddify_vpn.ui.screens.AppsScreen
import com.yasliks.easy_hiddify_vpn.ui.screens.LogScreen
import com.yasliks.easy_hiddify_vpn.ui.screens.MainScreen
import com.yasliks.easy_hiddify_vpn.ui.screens.SettingsScreen
import com.yasliks.easy_hiddify_vpn.ui.theme.EasyHiddifyVpnTheme
import com.yasliks.easy_hiddify_vpn.vm.AppsViewModel
import com.yasliks.easy_hiddify_vpn.vm.ServerViewModel
import com.yasliks.hiddify_library_lib.EasyHiddify
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val hiddify get() = EasyHiddify.instance

    private val appsViewModel: AppsViewModel by viewModels()
    private val serverViewModel: ServerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            EasyHiddifyVpnTheme {
                var currentPage: AppRoute by remember {
                    mutableStateOf(AppRoute.Main)
                }

                val bottomItems = listOf(
                    BottomNavItem.MainBottomItem,
                    BottomNavItem.ApplicationsBottomItem,
                    BottomNavItem.SettingsBottomItem,
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            bottomItems.forEach { screen ->
                                NavigationBarItem(
                                    selected = currentPage.getActiveBottomTab() == screen.appRoute,
                                    onClick = {
                                        currentPage = screen.appRoute
                                    },
                                    icon = {
                                        Icon(
                                            screen.icon,
                                            contentDescription = null,
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(screen.stringId),
                                        )
                                    },
                                )
                            }
                        }
                    },
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .consumeWindowInsets(paddingValues)
                            .statusBarsPadding()
                    ) {
                        when (currentPage) {
                            is AppRoute.Main -> {
                                MainScreen(
                                    appsViewModel = appsViewModel,
                                    hiddify = hiddify,
                                    serverViewModel = serverViewModel,
                                )
                            }

                            is AppRoute.Apps -> {
                                AppsScreen(
                                    appsViewModel = appsViewModel,
                                    hiddify = hiddify,
                                )
                            }

                            is AppRoute.Settings -> {
                                SettingsScreen(
                                    hiddify = hiddify,
                                    onOpenLogs = {
                                        currentPage = AppRoute.Logs
                                    }
                                )
                            }

                            is AppRoute.Logs -> {
                                BackHandler {
                                    currentPage = AppRoute.Settings
                                }
                                LogScreen(
                                    hiddify = hiddify,
                                    onBack = {
                                        currentPage = AppRoute.Settings
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}