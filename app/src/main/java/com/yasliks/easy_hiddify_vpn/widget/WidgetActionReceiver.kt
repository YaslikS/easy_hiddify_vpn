package com.yasliks.easy_hiddify_vpn.widget

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.yasliks.easy_hiddify_vpn.MainActivity
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.prefs.AppPrefs
import com.yasliks.easy_hiddify_vpn.utils.StartVpnResult
import com.yasliks.easy_hiddify_vpn.utils.VpnStarter
import com.yasliks.hiddify_library_lib.EasyHiddify
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class WidgetActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var vpnStarter: VpnStarter

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AppPrefs.RECEIVER_TOGGLE_VPN) return

        val hiddify = EasyHiddify.instance
        if (hiddify.state.connected.value) {
            hiddify.stopVpn()
            return
        }

        if (!hasRequiredPermissions(context)) {
            openMainActivity(context, AppPrefs.SHOW_PERMISSION_DIALOG)
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val result = vpnStarter.startLastServer(
                    hiddify = hiddify,
                    disconnectText = context.getString(R.string.push_button_text),
                )
                if (result is StartVpnResult.NoServers) {
                    withContext(Dispatchers.Main) {
                        openMainActivity(context, AppPrefs.EMPTY_SERVER_DIALOG)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start VPN from widget", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun hasRequiredPermissions(context: Context): Boolean {
        val hasVpnPermission = VpnService.prepare(context) == null
        val hasNotificationPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    /* context = */ context,
                    /* permission = */ Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        return hasVpnPermission && hasNotificationPermission
    }

    private fun openMainActivity(context: Context, extra: String) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(extra, true)
        }
        context.startActivity(launchIntent)
    }

    private companion object {
        const val TAG = "WidgetActionReceiver"
    }
}
