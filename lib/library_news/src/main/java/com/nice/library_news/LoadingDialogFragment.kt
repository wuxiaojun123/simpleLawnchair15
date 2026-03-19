package com.nice.library_news

import android.app.Dialog
import android.app.DialogFragment
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

class LoadingDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity ?: error("Activity is null")
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(24), dp(20), dp(24), dp(20))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(18).toFloat()
                setColor(Color.WHITE)
            }
        }
        container.addView(ProgressBar(context))
        container.addView(TextView(context).apply {
            text = "Loading more news..."
            textSize = 14f
            setTextColor(Color.parseColor("#303030"))
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, 0)
        })

        return Dialog(context).apply {
            setContentView(
                container,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun dp(value: Int): Int {
        val density = checkNotNull(activity).resources.displayMetrics.density
        return (value * density).toInt()
    }

    companion object {
        const val TAG = "LoadingDialogFragment"
    }
}
