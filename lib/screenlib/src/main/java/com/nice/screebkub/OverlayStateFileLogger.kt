package com.nice.screebkub

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OverlayStateFileLogger {
    private const val TAG = "OverlayStateFileLogger"
    private const val LOG_DIR = "debug_logs"
    private const val LOG_FILE = "overlay_state.txt"
    private const val MAX_BYTES = 512 * 1024L
    private val timestampFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val lock = Any()

    fun log(context: Context, tag: String, message: String) {
        Log.d(tag, message)
        val appContext = context.applicationContext
        synchronized(lock) {
            runCatching {
                val file = getLogFile(appContext)
                rotateIfNeeded(file)
                file.appendText("${timestampFormat.format(Date())} $tag $message\n")
            }.onFailure {
                Log.e(TAG, "Failed to write overlay state log", it)
            }
        }
    }

    fun getLogFile(context: Context): File {
        val dir = File(context.applicationContext.filesDir, LOG_DIR).apply { mkdirs() }
        return File(dir, LOG_FILE)
    }

    private fun rotateIfNeeded(file: File) {
        if (file.exists() && file.length() >= MAX_BYTES) {
            file.writeText(
                "${timestampFormat.format(Date())} $TAG log rotated after reaching ${file.length()} bytes\n",
            )
        }
    }
}
