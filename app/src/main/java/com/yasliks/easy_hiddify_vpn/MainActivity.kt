package com.yasliks.easy_hiddify_vpn

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.yasliks.easy_hiddify_vpn.prefs.AppPrefs
import com.yasliks.easy_hiddify_vpn.ui.AppContent
import com.yasliks.easy_hiddify_vpn.utils.ImportDeepLinkParser
import com.yasliks.easy_hiddify_vpn.utils.hasVpnStartPermissions
import com.yasliks.easy_hiddify_vpn.vm.AppsViewModel
import com.yasliks.easy_hiddify_vpn.vm.ImportViewModel
import com.yasliks.easy_hiddify_vpn.vm.MainViewModel
import com.yasliks.easy_hiddify_vpn.vm.ServerViewModel
import com.yasliks.easy_hiddify_vpn.vm.SettingsViewModel
import com.yasliks.hiddify_library_lib.EasyHiddify
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val hiddify get() = EasyHiddify.instance

    private val mainViewModel: MainViewModel by viewModels()
    private val appsViewModel: AppsViewModel by viewModels()
    private val serverViewModel: ServerViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val importViewModel: ImportViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent?.action == AppPrefs.RECEIVER_QUICK_CONNECT) {
            if (savedInstanceState == null) launchQuickConnect()
            finish()
            return
        }
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            handleIntent(intent = intent)
            if (intent?.action != Intent.ACTION_VIEW) {
                mainViewModel.checkAutoStart(
                    hiddify = hiddify,
                    hasPermissions = hasVpnStartPermissions(),
                    disconnectText = getString(R.string.push_button_text),
                )
            }
        }

        setContent {
            AppContent(
                hiddify = hiddify,
                mainViewModel = mainViewModel,
                appsViewModel = appsViewModel,
                serverViewModel = serverViewModel,
                settingsViewModel = settingsViewModel,
                importViewModel = importViewModel,
                onDeleteSubscription = ::deleteSubscription,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == AppPrefs.RECEIVER_QUICK_CONNECT) {
            launchQuickConnect()
        } else {
            handleIntent(intent = intent)
        }
    }

    private fun launchQuickConnect() {
        startActivity(
            Intent(this, QuickConnectActivity::class.java).apply {
                action = AppPrefs.RECEIVER_QUICK_CONNECT
            },
        )
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.action == Intent.ACTION_VIEW) {
            ImportDeepLinkParser.parse(link = intent.dataString)?.let { request ->
                importViewModel.open(
                    input = request.input,
                    kind = request.kind,
                )
            }
            return
        }
        if (intent.getBooleanExtra(AppPrefs.SHOW_PERMISSION_DIALOG, false)) {
            mainViewModel.setShowPermissionDialog(show = true)
            intent.removeExtra(AppPrefs.SHOW_PERMISSION_DIALOG)
        }
        if (intent.getBooleanExtra(AppPrefs.EMPTY_SERVER_DIALOG, false)) {
            mainViewModel.setShowEmptyServerDialog(show = true)
            intent.removeExtra(AppPrefs.EMPTY_SERVER_DIALOG)
        }
    }

    private fun deleteSubscription(url: String) {
        importViewModel.deleteSubscriptionFromMain(
            url = url,
            onError = {
                Toast.makeText(
                    this,
                    R.string.subscription_delete_failed,
                    Toast.LENGTH_SHORT,
                ).show()
            },
        )
    }
}
