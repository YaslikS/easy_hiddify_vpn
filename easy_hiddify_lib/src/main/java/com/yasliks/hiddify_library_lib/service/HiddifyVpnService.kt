package com.yasliks.hiddify_library_lib.service

import android.annotation.SuppressLint
import android.content.Intent
import android.net.VpnService
import androidx.annotation.DrawableRes
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
import com.yasliks.hiddify_library_lib.prefs.HiddifyPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@SuppressLint("VpnServicePolicy")
class HiddifyVpnService : VpnService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var commandServer: CommandServer? = null
    private var commandClient: CommandClient? = null

    private val sdk get() = EasyHiddify.instance

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action == HiddifyPrefs.ACTION_STOP_VPN) {
            stopVpnInternal()
            return START_NOT_STICKY
        }

        val configContent = intent?.getStringExtra(
            /* name = */ HiddifyPrefs.CONFIG_CONTENT,
        ) ?: ""
        val serverName = intent?.getStringExtra(
            /* name = */ HiddifyPrefs.NAME_SERVER,
        ) ?: HiddifyPrefs.VPN
        val icon = intent?.getIntExtra(
            /* name = */ HiddifyPrefs.ICON_PUSH,
            /* defaultValue = */ 0,
        ) ?: 0

        if (configContent.isNotEmpty()) {
            startVpn(
                configContent = configContent,
                serverName = serverName,
                icon = icon,
            )
        }
        return START_STICKY
    }

    /**
     * Starts a VPN connection using the transmitted configuration
     *
     * @param configContent connection configuration string
     * @param serverName the name of the notification server
     */
    private fun startVpn(
        configContent: String,
        serverName: String,
        @DrawableRes icon: Int,
    ) {
        val notification = sdk.notifications.createNotification(
            serverName = serverName,
            icon = icon,
        )
        startForeground(HiddifyPrefs.NOTIFICATION_ID, notification)

        serviceScope.launch {
            try {
                val hiddifyConfig = sdk.generator.generateConfig(configContent)

                Libbox.setup(SetupOptions().apply {
                    basePath = sdk.utils.getWorkingDir()
                    workingPath = sdk.utils.getWorkingDir()
                    tempPath = sdk.utils.getTempDir()
                    commandServerListenPort = HiddifyPrefs.COMMAND_SERVER_LISTEN_PORT
                    commandServerSecret = sdk.utils.generateSecret()
                    fixAndroidStack = true
                })

                commandServer = Libbox.newCommandServer(
                    /* handler = */ HiddifyCommandHandler(this@HiddifyVpnService),
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

    /**
     * Initializes the client and starts the connection
     */
    private fun setupCommandClient() {
        try {
            val options = CommandClientOptions()
                .apply {
                    statusInterval = HiddifyPrefs.STATUS_INTERVAL
                }
            commandClient = Libbox.newCommandClient(
                /* handler = */ HiddifyClientHandler(this),
                /* options = */ options,
            )
            commandClient?.connect()
        } catch (e: Exception) {
        }
    }

    /**
     * Notifies the VPN connection status
     *
     * @param isConnected transmitted connection status
     */
    private fun notifyStateChange(isConnected: Boolean) {
        val intent = Intent(HiddifyPrefs.ACTION_VPN_STATE).apply {
            putExtra(HiddifyPrefs.EXTRA_IS_CONNECTED, isConnected)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    /**
     * Closes the VPN connection
     */
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
                Thread.sleep(HiddifyPrefs.DELAY_BEFORE_EXIT)
                exitProcess(0)
            }
        }.start()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}