package com.android.launcher3.rightscreen

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.android.launcher3.Launcher
import com.android.launcher3.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RightScreenFragment : android.app.Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.right_screen_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val launcher = Launcher.getLauncher(view.context)
        view.findViewById<TextView>(R.id.right_screen_status)?.text = view.context.getString(
            R.string.right_screen_status,
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
        )

        view.findViewById<Button>(R.id.right_screen_open_settings)?.setOnClickListener {
            val intent = Intent(Intent.ACTION_APPLICATION_PREFERENCES)
                .setPackage(view.context.packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            launcher.startActivitySafely(it, intent, null)
        }

        view.findViewById<Button>(R.id.right_screen_show_toast)?.setOnClickListener {
            Toast.makeText(view.context, R.string.right_screen_toast_message, Toast.LENGTH_SHORT)
                .show()
        }
    }
}
