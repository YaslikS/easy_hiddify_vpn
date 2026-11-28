package com.yasliks.easy_hiddify_vpn.utils

import androidx.datastore.core.DataStore
import com.yas.database.repo.ServerRepo
import com.yasliks.database.data.SettingsData
import com.yasliks.hiddify_library_lib.EasyHiddify
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of a request to start the VPN.
 */
sealed interface StartVpnResult {

    data object Started : StartVpnResult

    data object NoServers : StartVpnResult
}

/**
 * Single entry point for starting the VPN.
 *
 * Every caller (main screen, quick settings tile, widget, quick connect) must use
 * this class so that server selection, selected apps and disconnect text are
 * resolved in one place.
 */
@Singleton
class VpnStarter @Inject constructor(
    private val serverRepo: ServerRepo,
    private val installedApps: InstalledAppsSync,
    private val settings: DataStore<SettingsData>,
) {

    /**
     * Starts the VPN with the given server config.
     */
    suspend fun startServer(
        hiddify: EasyHiddify,
        config: String,
        serverName: String,
        serverId: Int,
        disconnectText: String,
    ): StartVpnResult {
        hiddify.startVpn(
            configStr = config,
            serverName = serverName,
            serverId = serverId,
            appsList = installedApps.selectedPackages(),
            isEnabledApps = true,
            disconnectText = disconnectText,
        )
        return StartVpnResult.Started
    }

    /**
     * Starts the VPN with the last used server, falling back to the first saved one.
     */
    suspend fun startLastServer(
        hiddify: EasyHiddify,
        disconnectText: String,
    ): StartVpnResult {
        val lastServerId = settings.data.first().lastServerId
        val servers = serverRepo.getAll()
        val server = servers.find { it.id == lastServerId } ?: servers.firstOrNull()
            ?: return StartVpnResult.NoServers

        return startServer(
            hiddify = hiddify,
            config = server.config,
            serverName = server.name,
            serverId = server.id,
            disconnectText = disconnectText,
        )
    }
}
