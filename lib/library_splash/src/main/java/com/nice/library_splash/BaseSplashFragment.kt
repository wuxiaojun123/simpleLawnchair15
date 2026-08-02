package com.nice.library_splash

import androidx.fragment.app.Fragment

abstract class BaseSplashFragment : Fragment() {

    protected fun finishSplash() {
        (requireActivity() as SplashNavigator).finishSplash()
    }
}
