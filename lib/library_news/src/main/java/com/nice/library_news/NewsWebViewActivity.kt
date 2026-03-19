package com.nice.library_news

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

class NewsWebViewActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var loadingView: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_webview)

        webView = checkNotNull(findViewById(R.id.webView))
        loadingView = checkNotNull(findViewById(R.id.loadingView))
        val backButton = checkNotNull(findViewById<TextView>(R.id.backButton))

        val url = normalizeUrl(intent.getStringExtra(EXTRA_URL).orEmpty())
        backButton.setOnClickListener { onBackPressed() }

        if (url.isBlank()) {
            Toast.makeText(this, "Invalid news link", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val targetUrl = normalizeUrl(request?.url?.toString().orEmpty())
                return if (targetUrl.isBlank()) {
                    false
                } else {
                    view?.loadUrl(targetUrl)
                    true
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                loadingView.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                loadingView.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                if (request?.isForMainFrame == true) {
                    loadingView.visibility = View.GONE
                    Toast.makeText(
                        this@NewsWebViewActivity,
                        error?.description?.toString() ?: "Failed to open news page",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
        webView.webChromeClient = WebChromeClient()
        webView.loadUrl(url)
    }

    private fun normalizeUrl(url: String): String {
        if (url.isBlank()) return url
        return if (url.startsWith("http://")) {
            "https://" + url.removePrefix("http://")
        } else {
            url
        }
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.webChromeClient = null
            webView.destroy()
        }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }
}
