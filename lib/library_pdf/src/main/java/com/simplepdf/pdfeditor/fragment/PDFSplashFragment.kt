package com.simplepdf.pdfeditor.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nice.library_splash.BaseSplashFragment
import com.simplepdf.pdfeditor.R

class PDFSplashFragment : BaseSplashFragment() {

    private val handler = Handler(Looper.getMainLooper())
    private val openChoiceRunnable = Runnable { openFallbackActivity() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_pdf_splash, container, false)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(openChoiceRunnable, SPLASH_DELAY_MS)
    }

    override fun onPause() {
        handler.removeCallbacks(openChoiceRunnable)
        super.onPause()
    }

    override fun onDestroyView() {
        handler.removeCallbacks(openChoiceRunnable)
        super.onDestroyView()
    }

    companion object {
        private const val SPLASH_DELAY_MS = 3000L
    }
}