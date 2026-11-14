package com.yasliks.easy_hiddify_vpn.vm

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yas.database.repo.AppsRepo
import com.yas.database.repo.ServerRepo
import com.yasliks.database.data.SettingsData
import com.yasliks.easy_hiddify_vpn.ui.components.base.AppRoute
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
    private val appsRepo: AppsRepo,
    private val serverRepo: ServerRepo,
) : ViewModel() {

    private val _currentRoute = MutableStateFlow(AppRoute.Main.route)
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    private val _showEmptyServerDialog = MutableStateFlow(false)
    val showEmptyServerDialog: StateFlow<Boolean> = _showEmptyServerDialog.asStateFlow()

    fun navigateTo(route: String) {
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
            val settingsData = settings.data.first()

            if (!isConnected && settingsData.autoStartVpn && hasPermissions) {
                startVpnIfPossible(
                    hiddify = hiddify,
                    lastServerId = settingsData.lastServerId,
                    disconnectText = disconnectText,
                    showEmptyDialogIfNone = false,
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
            val isConnected = hiddify.state.connected.value

            if (isConnected) {
                hiddify.stopVpn()
            } else {
                if (!hasPermissions) {
                    _showPermissionDialog.value = true
                    return@launch
                }

                val settingsData = settings.data.first()
                startVpnIfPossible(
                    hiddify = hiddify,
                    lastServerId = settingsData.lastServerId,
                    disconnectText = disconnectText,
                    showEmptyDialogIfNone = true,
                )
            }
        }
    }

    private suspend fun startVpnIfPossible(
        hiddify: EasyHiddify,
        lastServerId: Int,
        disconnectText: String,
        showEmptyDialogIfNone: Boolean,
    ) {
        val servers = serverRepo.getAll()
        val targetServer = servers.find {
            it.id == lastServerId
        } ?: servers.firstOrNull()

        if (targetServer != null) {
            val selectedApps = appsRepo.getAll()
                .filter { it.isSelected }
                .map { it.packageName }

            hiddify.startVpn(
                configStr = targetServer.config,
                serverName = targetServer.name,
                serverId = targetServer.id,
                appsList = selectedApps,
                isEnabledApps = true,
                disconnectText = disconnectText,
            )
        } else {
            if (showEmptyDialogIfNone) {
                _showEmptyServerDialog.value = true
            }
        }
    }
}