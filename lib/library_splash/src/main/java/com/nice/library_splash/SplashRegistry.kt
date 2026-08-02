package com.nice.library_splash

import androidx.fragment.app.Fragment

object SplashRegistry {

    @Volatile
    private var fragmentFactory: SplashFragmentFactory = SplashFragmentFactory {
        DefaultSplashFragment()
    }

    @JvmStatic
    fun setFragmentFactory(factory: SplashFragmentFactory) {
        fragmentFactory = factory
    }

    @JvmStatic
    fun resetFragmentFactory() {
        fragmentFactory = SplashFragmentFactory { DefaultSplashFragment() }
    }

    internal fun createFragment(): Fragment = fragmentFactory.createFragment()
}
