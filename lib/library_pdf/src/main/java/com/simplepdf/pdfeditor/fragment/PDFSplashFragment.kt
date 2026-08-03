package com.simplepdf.pdfeditor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.nice.library_splash.BaseSplashFragment
import com.simplepdf.pdfeditor.R

class PDFSplashFragment : BaseSplashFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_pdf_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.requireViewById<Button>(R.id.buttonSetDefaultLauncher).setOnClickListener {
            requestSetDefaultLauncher()
        }
        view.requireViewById<Button>(R.id.buttonOpenPdf).setOnClickListener {
            openFallbackActivity()
        }
    }
}
