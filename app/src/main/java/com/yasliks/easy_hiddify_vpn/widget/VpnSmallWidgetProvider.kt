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

class VpnSmallWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val isConnected = EasyHiddify.instance.state.connected.value
        updateAllWidgets(
            context = context,
            appWidgetManager = appWidgetManager,
            appWidgetIds = appWidgetIds,
            isConnected = isConnected,
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == HiddifyPrefs.ACTION_VPN_STATE) {
            val isConnected = intent.getBooleanExtra(HiddifyPrefs.EXTRA_IS_CONNECTED, false)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                /* provider = */ ComponentName(
                    /* pkg = */ context,
                    /* cls = */ VpnSmallWidgetProvider::class.java,
                )
            )
            updateAllWidgets(
                context = context,
                appWidgetManager = appWidgetManager,
                appWidgetIds = appWidgetIds,
                isConnected = isConnected,
            )
        }
    }

    private fun updateAllWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        isConnected: Boolean,
    ) {
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_vpn_small)

            val statusText = if (isConnected) {
                context.getString(R.string.widget_connected)
            } else {
                context.getString(R.string.widget_disconnected)
            }
            views.setTextViewText(R.id.widget_tv_status, statusText)

            views.setImageViewResource(
                /* viewId = */ R.id.widget_btn_icon,
                /* srcId = */ if (isConnected) {
                    R.drawable.widget_icon_active
                } else {
                    R.drawable.widget_icon_inactive
                }
            )

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
}