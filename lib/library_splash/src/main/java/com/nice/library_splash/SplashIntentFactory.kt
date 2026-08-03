package com.nice.library_splash

import android.content.Context
import android.content.Intent

fun interface SplashIntentFactory {
    fun createIntent(context: Context): Intent
}