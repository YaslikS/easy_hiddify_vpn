package com.yasliks.easy_hiddify_vpn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.yasliks.easy_hiddify_vpn.prefs.AppPrefs
import com.yasliks.easy_hiddify_vpn.utils.StartVpnResult
import com.yasliks.easy_hiddify_vpn.utils.VpnStarter
import com.yasliks.easy_hiddify_vpn.utils.hasVpnStartPermissions
import com.yasliks.hiddify_library_lib.EasyHiddify
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuickConnectActivity : ComponentActivity() {

    @Inject
    lateinit var vpnStarter: VpnStarter
    private var handled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action != AppPrefs.RECEIVER_QUICK_CONNECT) finish()
    }

    override fun onPostResume() {
        super.onPostResume()
        if (handled || isFinishing) return
        handled = true
        lifecycleScope.launch {
            try {
                toggleVpn()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.e(TAG, "Failed to toggle VPN from the launcher shortcut", error)
            } finally {
                finish()
            }
        }
    }

    private suspend fun toggleVpn() {
        val hiddify = EasyHiddify.instance
        if (hiddify.state.connected.value) {
            hiddify.stopVpn()
            return
        }
        if (!hasVpnStartPermissions()) {
            openMainActivity(extra = AppPrefs.SHOW_PERMISSION_DIALOG)
            return
        }
        when (vpnStarter.startLastServer(
            hiddify = hiddify,
            disconnectText = getString(R.string.push_button_text),
        )) {
            StartVpnResult.Started -> Unit
            StartVpnResult.NoServers -> openMainActivity(extra = AppPrefs.EMPTY_SERVER_DIALOG)
        }
    }

    private fun openMainActivity(extra: String) {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(extra, true)
            },
        )
    }

    private companion object {
        const val TAG = "QuickConnectActivity"
    }
}
