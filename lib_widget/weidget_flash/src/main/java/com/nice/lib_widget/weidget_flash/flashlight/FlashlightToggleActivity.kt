package com.nice.lib_widget.weidget_flash.flashlight

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.widget.Toast
import com.nice.lib_widget.weidget_flash.R

class FlashlightToggleActivity : Activity() {

    private var permissionRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionRequested = savedInstanceState?.getBoolean(KEY_PERMISSION_REQUESTED) ?: false
        handleToggleRequest()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_PERMISSION_REQUESTED, permissionRequested)
        super.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            handleToggleRequest()
        } else {
            finish()
        }
    }

    private fun handleToggleRequest() {
        val controller = FlashlightController(this)
        if (!controller.hasCameraPermission()) {
            if (!permissionRequested) {
                permissionRequested = true
                requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            } else {
                Toast.makeText(this, R.string.widget_flashlight_permission_required, Toast.LENGTH_SHORT).show()
                finish()
            }
            return
        }

        val result = controller.toggle()
        if (result.isFailure) {
            Toast.makeText(this, R.string.widget_flashlight_toggle_failed, Toast.LENGTH_SHORT).show()
        }
        updateWidgets()
        finish()
    }

    private fun updateWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val componentName = ComponentName(this, FlashlightWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        FlashlightWidgetProvider.updateWidgets(this, appWidgetManager, appWidgetIds)
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1001
        private const val KEY_PERMISSION_REQUESTED = "permission_requested"
    }
}
