package com.yasliks.hiddify_library_lib.service

import android.annotation.SuppressLint
import android.content.Intent
import android.net.VpnService
import com.hiddify.core.libbox.CommandClient
import com.hiddify.core.libbox.CommandClientOptions
import com.hiddify.core.libbox.CommandServer
import com.hiddify.core.libbox.Libbox
import com.hiddify.core.libbox.OverrideOptions
import com.hiddify.core.libbox.SetupOptions
import com.yasliks.hiddify_library_lib.EasyHiddify
import com.yasliks.hiddify_library_lib.core.HiddifyClientHandler
import com.yasliks.hiddify_library_lib.core.HiddifyCommandHandler
import com.yasliks.hiddify_library_lib.core.HiddifyPlatform
import com.yasliks.hiddify_library_lib.utils.HiddifyNotificationUtils
import com.yasliks.hiddify_library_lib.utils.HiddifyState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@SuppressLint("VpnServicePolicy")
class HiddifyVpnService : VpnService() {

    companion object {
        const val ACTION_STOP = "com.yasliks.hiddify.STOP"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var commandServer: CommandServer? = null
    private var commandClient: CommandClient? = null

    private val sdk get() = EasyHiddify.instance

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopVpnInternal()
            return START_NOT_STICKY
        }

        val configContent = intent?.getStringExtra("config_content") ?: ""
        val serverName = intent?.getStringExtra("name_server") ?: "VPN"

        if (configContent.isNotEmpty()) {
            startVpn(configContent, serverName)
        }
        return START_STICKY
    }

    private fun startVpn(
        configContent: String,
        serverName: String,
    ) {
        val notification = sdk.notifications.createNotification(serverName)
        startForeground(HiddifyNotificationUtils.NOTIFICATION_ID, notification)

        serviceScope.launch {
            try {
                val hiddifyConfig = sdk.generator.generateConfig(configContent)

                Libbox.setup(SetupOptions().apply {
                    basePath = sdk.utils.getWorkingDir()
                    workingPath = sdk.utils.getWorkingDir()
                    tempPath = sdk.utils.getTempDir()
                    commandServerListenPort = 6756
                    commandServerSecret = sdk.utils.generateSecret()
                    fixAndroidStack = true
                })

                commandServer = Libbox.newCommandServer(
                    /* handler = */ HiddifyCommandHandler(),
                    /* platformInterface = */ HiddifyPlatform(this@HiddifyVpnService),
                )
                commandServer?.start()
                commandServer?.startOrReloadService(
                    /* configContent = */ hiddifyConfig,
                    /* options = */ OverrideOptions(),
                )

                notifyStateChange(true)
                setupCommandClient()
            } catch (e: Exception) {
                stopVpnInternal()
            }
        }
    }

    private fun setupCommandClient() {
        try {
            val options = CommandClientOptions().apply { statusInterval = 1000 }
            commandClient = Libbox.newCommandClient(
                /* handler = */ HiddifyClientHandler(),
                /* options = */ options,
            )
            commandClient?.connect()
        } catch (e: Exception) {
        }
    }

    private fun notifyStateChange(isConnected: Boolean) {
        val intent = Intent(HiddifyState.ACTION_VPN_STATE).apply {
            putExtra(HiddifyState.EXTRA_IS_CONNECTED, isConnected)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun stopVpnInternal() {
        notifyStateChange(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        Thread {
            try {
                commandClient?.disconnect()
                commandServer?.closeService()
                commandServer?.close()
            } catch (e: Exception) {
            } finally {
                stopSelf()
                Thread.sleep(200)
                exitProcess(0)
            }
        }.start()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}