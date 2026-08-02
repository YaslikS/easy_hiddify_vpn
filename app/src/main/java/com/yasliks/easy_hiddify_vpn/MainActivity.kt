package com.yasliks.easy_hiddify_vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yasliks.easy_hiddify_vpn.ui.components.BottomNavItems
import com.yasliks.easy_hiddify_vpn.ui.screens.AppsScreen
import com.yasliks.easy_hiddify_vpn.ui.screens.MainScreen
import com.yasliks.easy_hiddify_vpn.vm.AppsViewModel
import com.yasliks.hiddify_library_lib.EasyHiddify
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val hiddify get() = EasyHiddify.instance

    private val appsViewModel: AppsViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                var currentPage by remember {
                    mutableStateOf(BottomNavItems.MainBottomItem.route)
                }

                val bottomItems = listOf(
                    BottomNavItems.MainBottomItem,
                    BottomNavItems.ApplicationsBottomItem,
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            bottomItems.forEachIndexed { _, screen ->
                                NavigationBarItem(
                                    selected = currentPage == screen.route,
                                    onClick = {
                                        currentPage = screen.route
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
                    ) {
                        when (currentPage) {
                            BottomNavItems.MainBottomItem.route -> {
                                MainScreen(
                                    appsViewModel = appsViewModel,
                                    hiddify = hiddify,
                                )
                            }

                            BottomNavItems.ApplicationsBottomItem.route -> {
                                AppsScreen(
                                    appsViewModel = appsViewModel,
                                    hiddify = hiddify,
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }
        }
    }
}