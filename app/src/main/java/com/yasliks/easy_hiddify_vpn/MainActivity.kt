package com.yasliks.easy_hiddify_vpn

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
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
            val isConnected by hiddify.state.connected.collectAsState()
            var currentPage: AppRoute by remember { mutableStateOf(AppRoute.Main) }

            SystemBarsManager(
                isMainScreen = currentPage is AppRoute.Main,
                isConnected = isConnected
            )

            EasyHiddifyVpnTheme(isConnected = isConnected) {
                val bottomItems = listOf(
                    BottomNavItem.MainBottomItem,
                    BottomNavItem.ApplicationsBottomItem,
                    BottomNavItem.SettingsBottomItem,
                )

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            bottomItems.forEach { screen ->
                                NavigationBarItem(
                                    selected = currentPage.getActiveBottomTab() == screen.appRoute,
                                    onClick = { currentPage = screen.appRoute },
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(text = stringResource(screen.stringId)) }
                                )
                            }
                        }
                    },
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = paddingValues.calculateBottomPadding())
                    ) {
                        when (currentPage) {
                            is AppRoute.Main -> MainScreen(appsViewModel, serverViewModel, hiddify)
                            is AppRoute.Apps -> AppsScreen(hiddify, appsViewModel)
                            is AppRoute.Settings -> SettingsScreen(hiddify) { currentPage = AppRoute.Logs }
                            is AppRoute.Logs -> {
                                BackHandler { currentPage = AppRoute.Settings }
                                LogScreen(hiddify) { currentPage = AppRoute.Settings }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemBarsManager(isMainScreen: Boolean, isConnected: Boolean) {
    val view = LocalView.current
    val isDarkTheme = isSystemInDarkTheme()

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            val useDarkIcons = if (isMainScreen && isConnected) {
                false
            } else {
                !isDarkTheme
            }

            insetsController.isAppearanceLightStatusBars = useDarkIcons
            insetsController.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }
}