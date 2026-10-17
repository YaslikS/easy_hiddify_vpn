package com.yasliks.easy_hiddify_vpn.ui.components.base

sealed class AppRoute(val route: String) {
    data object Main : AppRoute("screenMain")
    data object Apps : AppRoute("screenApps")
    data object Settings : AppRoute("screenSettings")
    data object Logs : AppRoute("screenLogs")

    fun getActiveBottomTab(): AppRoute {
        return when (this) {
            is Logs -> Settings
            else -> this
        }
    }
}