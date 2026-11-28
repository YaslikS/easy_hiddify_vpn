package com.yasliks.easy_hiddify_vpn.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.yasliks.easy_hiddify_vpn.MainActivity
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.prefs.AppPrefs
import com.yasliks.easy_hiddify_vpn.utils.StartVpnResult
import com.yasliks.easy_hiddify_vpn.utils.VpnStarter
import com.yasliks.easy_hiddify_vpn.utils.hasVpnStartPermissions
import com.yasliks.hiddify_library_lib.EasyHiddify
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class VpnTileService : TileService() {

    @Inject
    lateinit var vpnStarter: VpnStarter

    @Inject
    lateinit var hiddify: EasyHiddify

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private var stateJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        stateJob?.cancel()
        stateJob = serviceScope.launch {
            hiddify.state.connected.collect { isConnected ->
                updateTileState(isConnected)
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        stateJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun updateTileState(isConnected: Boolean) {
        val tile = qsTile ?: return
        if (isConnected) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = getString(R.string.tile_vpn_on)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = getString(R.string.tile_secure)
            }
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.tile_vpn_off)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = getString(R.string.tile_tap_to_connect)
            }
        }
        tile.updateTile()
    }

    private suspend fun openMainActivity(reason: String? = null) {
        withContext(Dispatchers.Main) {
            val intent = Intent(this@VpnTileService, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                if (reason == AppPrefs.PERMISSION) {
                    putExtra(AppPrefs.SHOW_PERMISSION_DIALOG, true)
                }
                if (reason == AppPrefs.EMPTY_SERVER) {
                    putExtra(AppPrefs.EMPTY_SERVER_DIALOG, true)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    /* context = */ this@VpnTileService,
                    /* requestCode = */ 0,
                    /* intent = */ intent,
                    /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
        }
    }

    override fun onClick() {
        super.onClick()
        val isConnected = hiddify.state.connected.value

        if (isConnected) {
            hiddify.stopVpn()
        } else {
            if (!hasVpnStartPermissions()) {
                serviceScope.launch {
                    openMainActivity(reason = AppPrefs.PERMISSION)
                }
                updateTileState(false)
                return
            }

            serviceScope.launch(context = Dispatchers.IO) {
                val result = vpnStarter.startLastServer(
                    hiddify = hiddify,
                    disconnectText = getString(R.string.push_button_text),
                )
                if (result is StartVpnResult.NoServers) {
                    openMainActivity(reason = AppPrefs.EMPTY_SERVER)
                }
            }
        }
    }
}
