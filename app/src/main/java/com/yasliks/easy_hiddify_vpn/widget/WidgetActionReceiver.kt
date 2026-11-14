package com.yasliks.easy_hiddify_vpn.widget

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import com.yas.database.repo.AppsRepo
import com.yas.database.repo.ServerRepo
import com.yasliks.database.data.SettingsData
import com.yasliks.easy_hiddify_vpn.MainActivity
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.prefs.AppPrefs
import com.yasliks.hiddify_library_lib.EasyHiddify
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class WidgetActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var serverRepo: ServerRepo

    @Inject
    lateinit var appsRepo: AppsRepo

    @Inject
    lateinit var settings: DataStore<SettingsData>

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AppPrefs.RECEIVER_TOGGLE_VPN) {
            val hiddify = EasyHiddify.instance
            val isConnected = hiddify.state.connected.value

            if (isConnected) {
                hiddify.stopVpn()
            } else {
                val hasVpnPermission = VpnService.prepare(context) == null
                val hasNotificationPermission =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            /* context = */ context,
                            /* permission = */ Manifest.permission.POST_NOTIFICATIONS,
                        ) == PackageManager.PERMISSION_GRANTED
                    } else true

                if (!hasVpnPermission || !hasNotificationPermission) {
                    val launchIntent = Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        putExtra(AppPrefs.SHOW_PERMISSION_DIALOG, true)
                    }
                    context.startActivity(launchIntent)
                    return
                }

                runBlocking(Dispatchers.IO) {
                    val settingsData = settings.data.first()
                    val lastServerId = settingsData.lastServerId
                    val servers = serverRepo.getAll()
                    val targetServer =
                        servers.find { it.id == lastServerId } ?: servers.firstOrNull()

                    if (targetServer != null) {
                        val selectedApps =
                            appsRepo.getAll().filter { it.isSelected }.map { it.packageName }

                        hiddify.startVpn(
                            configStr = targetServer.config,
                            serverName = targetServer.name,
                            serverId = targetServer.id,
                            appsList = selectedApps,
                            isEnabledApps = true,
                            disconnectText = context.getString(R.string.push_button_text),
                        )
                    } else {
                        val launchIntent = Intent(context, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            putExtra(AppPrefs.EMPTY_SERVER_DIALOG, true)
                        }
                        context.startActivity(launchIntent)
                    }
                }
            }
        }
    }
}