package com.yasliks.hiddify_library_lib.core

import android.content.Context
import android.util.Log
import com.hiddify.core.libbox.*
import com.yasliks.easy_hiddify_lib.R
import com.yasliks.hiddify_library_lib.EasyHiddify
import com.yasliks.hiddify_library_lib.prefs.HiddifyPrefs

class HiddifyClientHandler(
    private val context: Context,
) : CommandClientHandler {

    private val sdk get() = EasyHiddify.instance

    override fun connected() {
        sdk.state.updateConnected(true)
    }

    override fun disconnected(message: String?) {
        sdk.state.updateConnected(false)
    }

    override fun writeLogs(messageList: LogIterator) {
        Log.d(
            /* tag = */ HiddifyPrefs.HIDDIFY,
            /* msg = */ context.getString(
                /* resId = */ R.string.received_logs_from_core,
                /* ...formatArgs = */ messageList.len(),
            ),
        )
        sdk.logger.append(messageList)
    }

    override fun writeStatus(message: StatusMessage) {
        sdk.state.updateStatus(message)
    }

    override fun writeGroups(message: OutboundGroupIterator) {
        val groups = mutableListOf<OutboundGroup>()
        while (message.hasNext()) {
            groups.add(message.next())
        }
        sdk.state.updateGroups(groups)
    }

    override fun writeConnectionEvents(events: ConnectionEvents) {

    }

    override fun initializeClashMode(modeList: StringIterator, currentMode: String) {}

    override fun updateClashMode(newMode: String) {}

    override fun setDefaultLogLevel(level: Int) {}

    override fun clearLogs() {
        sdk.logger.clear()
    }
}