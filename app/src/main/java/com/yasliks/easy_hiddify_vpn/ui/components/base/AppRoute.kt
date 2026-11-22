package com.yasliks.easy_hiddify_vpn.ui.components.base

sealed class AppRoute {
    data object Main : AppRoute()
    data object Apps : AppRoute()
    data object Settings : AppRoute()
    data object Logs : AppRoute()

    fun getActiveTab(): AppRoute {
        return when (this) {
            is Logs -> Settings
            else -> this
        }
    }
}
