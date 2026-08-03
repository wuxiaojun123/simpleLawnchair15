package com.nice.library_splash

import androidx.fragment.app.Fragment

abstract class BaseSplashFragment : Fragment() {

    protected fun finishSplash() {
        navigator.finishSplash()
    }

    protected fun openFallbackActivity() {
        navigator.openFallbackActivity()
    }

    protected fun requestSetDefaultLauncher() {
        navigator.requestSetDefaultLauncher()
    }

    private val navigator: SplashNavigator
        get() = requireActivity() as SplashNavigator
}
