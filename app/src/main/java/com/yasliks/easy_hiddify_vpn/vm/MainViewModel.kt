package com.yasliks.easy_hiddify_vpn.vm

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yasliks.database.data.SettingsData
import com.yasliks.easy_hiddify_vpn.ui.components.base.AppRoute
import com.yasliks.easy_hiddify_vpn.utils.StartVpnResult
import com.yasliks.easy_hiddify_vpn.utils.VpnStarter
import com.yasliks.hiddify_library_lib.EasyHiddify
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settings: DataStore<SettingsData>,
    private val vpnStarter: VpnStarter,
) : ViewModel() {

    private val _currentRoute = MutableStateFlow<AppRoute>(AppRoute.Main)
    val currentRoute: StateFlow<AppRoute> = _currentRoute.asStateFlow()

    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    private val _showEmptyServerDialog = MutableStateFlow(false)
    val showEmptyServerDialog: StateFlow<Boolean> = _showEmptyServerDialog.asStateFlow()

    fun navigateTo(route: AppRoute) {
        _currentRoute.value = route
    }

    fun setShowPermissionDialog(show: Boolean) {
        _showPermissionDialog.value = show
    }

    fun setShowEmptyServerDialog(show: Boolean) {
        _showEmptyServerDialog.value = show
    }

    fun checkAutoStart(
        hiddify: EasyHiddify,
        hasPermissions: Boolean,
        disconnectText: String,
    ) {
        viewModelScope.launch {
            val isConnected = hiddify.state.connected.value
            val autoStartVpn = settings.data.first().autoStartVpn

            if (!isConnected && autoStartVpn && hasPermissions) {
                vpnStarter.startLastServer(
                    hiddify = hiddify,
                    disconnectText = disconnectText,
                )
            }
        }
    }

    fun handleQuickConnect(
        hiddify: EasyHiddify,
        hasPermissions: Boolean,
        disconnectText: String,
    ) {
        viewModelScope.launch {
            if (hiddify.state.connected.value) {
                hiddify.stopVpn()
                return@launch
            }

            if (!hasPermissions) {
                _showPermissionDialog.value = true
                return@launch
            }

            if (vpnStarter.startLastServer(
                    hiddify = hiddify,
                    disconnectText = disconnectText,
                ) is StartVpnResult.NoServers
            ) {
                _showEmptyServerDialog.value = true
            }
        }
    }

    fun startServer(
        hiddify: EasyHiddify,
        config: String,
        serverName: String,
        serverId: Int,
        disconnectText: String,
    ) {
        viewModelScope.launch {
            vpnStarter.startServer(
                hiddify = hiddify,
                config = config,
                serverName = serverName,
                serverId = serverId,
                disconnectText = disconnectText,
            )
        }
    }
}
