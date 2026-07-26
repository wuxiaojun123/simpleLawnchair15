package com.nice.lib_widget.weidget_flash.flashlight

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.nice.lib_widget.weidget_flash.R

class FlashlightWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray,
        ) {
            for (appWidgetId in appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, createRemoteViews(context))
            }
        }

        private fun createRemoteViews(context: Context): RemoteViews {
            val enabled = FlashlightController(context).getSavedState()
            val statusRes = if (enabled) {
                R.string.widget_flashlight_on
            } else {
                R.string.widget_flashlight_off
            }
            val iconRes = if (enabled) {
                R.drawable.ic_widget_flashlight_on
            } else {
                R.drawable.ic_widget_flashlight_off
            }

            return RemoteViews(context.packageName, R.layout.widget_flashlight).apply {
                setTextViewText(R.id.flashlight_widget_status, context.getString(statusRes))
                setImageViewResource(R.id.flashlight_widget_icon, iconRes)
                setOnClickPendingIntent(R.id.flashlight_widget_root, createTogglePendingIntent(context))
            }
        }

        private fun createTogglePendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, FlashlightToggleActivity::class.java).apply {
                action = "com.nice.lib_widget.weidget_flash.action.TOGGLE_FLASHLIGHT"
            }
            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
