package com.yasliks.hiddify_library_lib.core

import android.util.Log
import com.hiddify.core.libbox.CommandServerHandler
import com.hiddify.core.libbox.SystemProxyStatus
import com.yasliks.hiddify_library_lib.EasyHiddify

class HiddifyCommandHandler : CommandServerHandler {

    private val sdk get() = EasyHiddify.instance

    override fun getSystemProxyStatus(): SystemProxyStatus {
        return SystemProxyStatus().apply {
            available = false
            enabled = false
        }
    }

    override fun serviceReload() {
        sdk.logger.append(2, "Core requested service reload")
    }

    override fun serviceStop() {
        sdk.logger.append(2, "Core requested service stop")
    }

    override fun setSystemProxyEnabled(enabled: Boolean) {

    }

    override fun writeDebugMessage(message: String) {
        Log.e("CORE", message)
        sdk.logger.append(1, "[Core Debug] $message")
    }
}