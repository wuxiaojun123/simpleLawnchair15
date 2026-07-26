package com.nice.lib_widget.weidget_flash.flashlight

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.core.content.ContextCompat

class FlashlightController(private val context: Context) {

    private val appContext = context.applicationContext
    private val cameraManager = appContext.getSystemService(CameraManager::class.java)
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(appContext, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun getSavedState(): Boolean {
        return prefs.getBoolean(KEY_TORCH_ENABLED, false)
    }

    fun toggle(): Result<Boolean> {
        val nextState = !getSavedState()
        return setEnabled(nextState)
    }

    fun setEnabled(enabled: Boolean): Result<Boolean> {
        return try {
            val cameraId = findFlashCameraId()
                ?: return Result.failure(IllegalStateException("No camera flash is available"))
            cameraManager.setTorchMode(cameraId, enabled)
            saveState(enabled)
            Result.success(enabled)
        } catch (e: CameraAccessException) {
            Result.failure(e)
        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    private fun findFlashCameraId(): String? {
        var fallbackCameraId: String? = null

        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            if (!hasFlash) continue

            if (fallbackCameraId == null) {
                fallbackCameraId = cameraId
            }

            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                return cameraId
            }
        }

        return fallbackCameraId
    }

    private fun saveState(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TORCH_ENABLED, enabled).apply()
    }

    companion object {
        private const val PREFS_NAME = "flashlight_widget"
        private const val KEY_TORCH_ENABLED = "torch_enabled"
    }
}
