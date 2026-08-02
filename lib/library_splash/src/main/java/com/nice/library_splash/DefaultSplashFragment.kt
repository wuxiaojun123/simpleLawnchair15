package com.nice.library_splash

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class DefaultSplashFragment : BaseSplashFragment() {

    private val handler = Handler(Looper.getMainLooper())
    private val finishRunnable = Runnable { finishSplash() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val context = requireContext()
        val contentView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
        }

        val iconSize = resources.getDimensionPixelSize(R.dimen.library_splash_icon_size)
        val iconView = ImageView(context).apply {
            setImageDrawable(context.applicationInfo.loadIcon(context.packageManager))
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize)
        }
        contentView.addView(iconView)

        val titleView = TextView(context).apply {
            text = context.applicationInfo.loadLabel(context.packageManager)
            textSize = 20f
            setTextColor(Color.rgb(27, 27, 31))
            includeFontPadding = false
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.library_splash_title_top_margin)
            }
        }
        contentView.addView(titleView)

        return contentView
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(finishRunnable, SPLASH_DELAY_MS)
    }

    override fun onPause() {
        handler.removeCallbacks(finishRunnable)
        super.onPause()
    }

    override fun onDestroyView() {
        handler.removeCallbacks(finishRunnable)
        super.onDestroyView()
    }

    companion object {
        private const val SPLASH_DELAY_MS = 800L
    }
}
