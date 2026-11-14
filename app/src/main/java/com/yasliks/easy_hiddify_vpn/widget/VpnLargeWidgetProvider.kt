package com.yasliks.easy_hiddify_vpn.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.yasliks.easy_hiddify_vpn.MainActivity
import com.yasliks.easy_hiddify_vpn.R
import com.yasliks.easy_hiddify_vpn.prefs.AppPrefs
import com.yasliks.hiddify_library_lib.EasyHiddify
import com.yasliks.hiddify_library_lib.prefs.HiddifyPrefs

class VpnLargeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val isConnected = EasyHiddify.instance.state.connected.value
        val zeroSpeed = context.getString(R.string.widget_zero_speed)
        updateAllWidgets(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetIds = appWidgetIds,
            isConnected = isConnected,
            down = zeroSpeed,
            up = zeroSpeed,
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            /* provider = */ ComponentName(
                /* pkg = */ context,
                /* cls = */ VpnLargeWidgetProvider::class.java,
            )
        )

        when (intent.action) {
            HiddifyPrefs.ACTION_VPN_STATE -> {
                val isConnected = intent.getBooleanExtra(
                    /* name = */ HiddifyPrefs.EXTRA_IS_CONNECTED,
                    /* defaultValue = */ false,
                )
                val zeroSpeed = context.getString(R.string.widget_zero_speed)
                updateAllWidgets(
                    context = context,
                    appWidgetManager = appWidgetManager,
                    appWidgetIds = appWidgetIds,
                    isConnected = isConnected,
                    down = zeroSpeed,
                    up = zeroSpeed,
                )
            }

            HiddifyPrefs.ACTION_VPN_TRAFFIC -> {
                val downSpeed = intent.getLongExtra(
                    /* name = */ HiddifyPrefs.EXTRA_DOWNLINK_SPEED,
                    /* defaultValue = */ 0L,
                )
                val upSpeed = intent.getLongExtra(
                    /* name = */ HiddifyPrefs.EXTRA_UPLINK_SPEED,
                    /* defaultValue = */ 0L,
                )
                updateAllWidgets(
                    context = context,
                    appWidgetManager = appWidgetManager,
                    appWidgetIds = appWidgetIds,
                    isConnected = true,
                    down = formatBytes(downSpeed) + "/s",
                    up = formatBytes(upSpeed) + "/s",
                )
            }
        }
    }

    private fun updateAllWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        isConnected: Boolean,
        down: String,
        up: String,
    ) {
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_vpn_large)

            val statusText = if (isConnected) {
                context.getString(R.string.widget_secure)
            } else {
                context.getString(R.string.widget_ready)
            }
            views.setTextViewText(R.id.widget_tv_status, statusText)

            views.setImageViewResource(
                /* viewId = */ R.id.widget_btn_icon,
                /* srcId = */
                if (isConnected) {
                    R.drawable.widget_icon_active
                } else {
                    R.drawable.widget_icon_inactive
                },
            )

            if (isConnected) {
                views.setTextViewText(R.id.widget_tv_down, down)
                views.setTextViewText(R.id.widget_tv_up, up)
            } else {
                val zeroSpeed = context.getString(R.string.widget_zero_speed)
                views.setTextViewText(R.id.widget_tv_down, zeroSpeed)
                views.setTextViewText(R.id.widget_tv_up, zeroSpeed)
            }

            val appIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val appPendingIntent = PendingIntent.getActivity(
                /* context = */ context,
                /* requestCode = */ 0,
                /* intent = */ appIntent,
                /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_root, appPendingIntent)

            val clickIntent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = AppPrefs.RECEIVER_TOGGLE_VPN
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                /* context = */ context,
                /* requestCode = */ 1,
                /* intent = */ clickIntent,
                /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_btn_icon, togglePendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
        val pre = "KMGTPE"[exp - 1]
        return String.format(
            locale = java.util.Locale.US,
            format = "%.1f %cB",
            bytes / Math.pow(1024.0, exp.toDouble()),
            pre
        )
    }
}