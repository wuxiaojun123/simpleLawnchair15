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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RightScreenFragment : Fragment() {
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
            setBackgroundColor(Color.parseColor("#FFF4E5"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }

        root.addView(
            TextView(context).apply {
                text = "RIGHT SCREEN"
                textSize = 34f
                setTextColor(Color.parseColor("#8A4B08"))
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

        content.addView(title("Right Screen", 28f, "#111111"))
        content.addView(body("This right screen content now lives in screenlib.", 8))
        content.addView(body("Right screen ready. Updated at ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())}.", 12, "#444444"))
        content.addView(title("This page is a real workspace screen hosting a fragment.", 16f, "#7A4B0F", 20))
        content.addView(card("Why this screen is different", "Unlike the left overlay, this page is inserted as the rightmost workspace page, so it behaves like a normal home screen page."))
        content.addView(title("Quick actions", 16f, "#111111", 24))
        content.addView(button("Open settings") {
            val intent = Intent(Intent.ACTION_APPLICATION_PREFERENCES)
                .setPackage(context.packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        })
        content.addView(button("Show demo message") {
            Toast.makeText(context, "Right screen fragment is active.", Toast.LENGTH_SHORT).show()
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

    private fun body(text: String, topMarginDp: Int, color: String = "#666666"): TextView {
        return TextView(checkNotNull(activity)).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.parseColor(color))
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
        private const val TAG = "RightScreenFragment"
    }
}
