package com.yasliks.hiddify_library_lib

import android.content.Context
import android.content.Intent
import com.hiddify.core.libbox.Libbox
import com.yasliks.hiddify_library_lib.service.HiddifyVpnService
import com.yasliks.hiddify_library_lib.utils.HiddifyConfigUtils
import com.yasliks.hiddify_library_lib.utils.HiddifyLoggerUtils
import com.yasliks.hiddify_library_lib.utils.HiddifyNotificationUtils
import com.yasliks.hiddify_library_lib.utils.HiddifyState
import com.yasliks.hiddify_library_lib.utils.HiddifyUtils
import go.Seq

class EasyHiddify private constructor(
    context: Context,
) {

    private val appContext = context.applicationContext

    val state = HiddifyState(appContext)
    val logger = HiddifyLoggerUtils()
    val generator = HiddifyConfigUtils()
    val utils = HiddifyUtils(appContext)
    val notifications = HiddifyNotificationUtils(appContext)

    companion object {
        @Volatile
        private var INSTANCE: EasyHiddify? = null

        /**
         * Initializes `EasyHiddify`
         *
         * @param context for library needs
         */
        fun init(context: Context): EasyHiddify {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val ctx = context.applicationContext

                    // Initializing the Go layer
                    Seq.setContext(ctx)
                    Libbox.touch()

                    val instance = EasyHiddify(ctx)
                    INSTANCE = instance
                    instance
                }
            }
        }

        /**
         * Global access `EasyHiddify`
         */
        val instance: EasyHiddify
            get() = INSTANCE ?: throw IllegalStateException(
                "EasyHiddify must be initialized in Application.onCreate using EasyHiddify.init(this)"
            )
    }

    /**
     * Launching a VPN via Intent
     *
     * @param configStr server configuration string
     * @param serverName the name of the server to display in the notification
     */
    fun startVpn(
        configStr: String,
        serverName: String = "Server",
    ) {
        val intent = Intent(
            /* packageContext = */ appContext,
            /* cls = */ HiddifyVpnService::class.java,
        ).apply {
            putExtra("config_content", configStr)
            putExtra("name_server", serverName)
        }
        appContext.startService(intent)
    }

    /**
     * Stopping VPN via Intent
     */
    fun stopVpn() {
        val intent = Intent(
            /* packageContext = */ appContext,
            /* cls = */ HiddifyVpnService::class.java,
        ).apply {
            action = HiddifyVpnService.ACTION_STOP
        }
        appContext.startService(intent)
    }
}