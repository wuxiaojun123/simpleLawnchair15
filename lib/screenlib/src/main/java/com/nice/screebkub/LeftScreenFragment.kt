package com.nice.screebkub

import android.app.Fragment
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LeftScreenFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        debugLog("onCreate savedInstanceState=${savedInstanceState != null}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        debugLog("onCreateView container=${container?.javaClass?.simpleName} savedInstanceState=${savedInstanceState != null}")
        val context = inflater.context
        val root = FrameLayout(context).apply {
            setBackgroundColor(Color.parseColor("#E8F0FE"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }

        root.addView(
            TextView(context).apply {
                text = "LEFT SCREEN"
                textSize = 34f
                setTextColor(Color.parseColor("#1A3C6E"))
                alpha = 0.16f
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER,
                )
            },
        )

        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        val statusView = TextView(context).apply {
            setTextColor(Color.parseColor("#444444"))
            textSize = 14f
        }

        content.addView(title("Minus One", 28f, "#111111"))
        content.addView(body("This left screen content now lives in screenlib.", 8))
        content.addView(statusView.apply {
            text = "Overlay attached. Updated at ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}."
        })
        content.addView(title("This page is rendered by LeftScreenOverlay.", 16f, "#24466F", 20))
        content.addView(card("How this demo is wired", "Workspace overscroll drives the overlay. The overlay hosts this view. This view only renders content and handles clicks."))
        content.addView(title("Quick actions", 16f, "#111111", 24))
        content.addView(button("Open settings") {
            val intent = Intent(Intent.ACTION_APPLICATION_PREFERENCES)
                .setPackage(context.packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        })
        content.addView(button("Open widgets") {
            (activity as? LeftScreenHostActions)?.openLeftScreenWidgets()
        }, buttonLayoutParams(12))
        content.addView(button("Wallpaper") {
            Toast.makeText(context, "Wallpaper entry can be wired here next.", Toast.LENGTH_SHORT).show()
        }, buttonLayoutParams(12))

        root.addView(
            ScrollView(context).apply {
                isFillViewport = true
                addView(content)
            },
        )
        return root
    }

    override fun onResume() {
        super.onResume()
        debugLog("onResume view=${view != null} parent=${view?.parent?.javaClass?.simpleName}")
    }

    override fun onPause() {
        debugLog("onPause")
        super.onPause()
    }

    override fun onDestroyView() {
        debugLog("onDestroyView")
        super.onDestroyView()
    }

    private fun title(text: String, sizeSp: Float, color: String, topMarginDp: Int = 0): TextView {
        return TextView(checkNotNull(activity)).apply {
            this.text = text
            textSize = sizeSp
            setTextColor(Color.parseColor(color))
            if (sizeSp >= 18f) {
                setTypeface(typeface, Typeface.BOLD)
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(topMarginDp)
            }
        }
    }

    private fun body(text: String, topMarginDp: Int): TextView {
        return TextView(checkNotNull(activity)).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.parseColor("#666666"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(topMarginDp)
            }
        }
    }

    private fun card(title: String, description: String): LinearLayout {
        return LinearLayout(checkNotNull(activity)).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            elevation = dp(2).toFloat()
            setPadding(dp(20), dp(20), dp(20), dp(20))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(24)
            }
            addView(title(title, 18f, "#111111"))
            addView(body(description, 8))
        }
    }

    private fun button(text: String, onClick: () -> Unit): Button {
        return Button(checkNotNull(activity)).apply {
            this.text = text
            setOnClickListener { onClick() }
            layoutParams = buttonLayoutParams()
        }
    }

    private fun buttonLayoutParams(topMarginDp: Int = 12): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            topMargin = dp(topMarginDp)
        }
    }

    private fun dp(value: Int): Int {
        return (value * checkNotNull(activity).resources.displayMetrics.density).toInt()
    }

    private fun debugLog(message: String) {
        activity?.applicationContext?.let {
            OverlayStateFileLogger.log(it, TAG, message)
        }
    }

    companion object {
        private const val TAG = "LeftScreenFragment"
    }
}
