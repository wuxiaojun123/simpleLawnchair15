package com.nice.library_splash

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

object SplashRegistry {

    @Volatile
    private var fragmentFactory: SplashFragmentFactory = SplashFragmentFactory {
        DefaultSplashFragment()
    }

    @Volatile
    private var targetActivityFactory: SplashIntentFactory? = null

    @Volatile
    private var fallbackActivityFactory: SplashIntentFactory? = null

    @Volatile
    private var defaultHomeFallbackActivityFactory: SplashIntentFactory? = null

    @JvmStatic
    fun setFragmentFactory(factory: SplashFragmentFactory) {
        fragmentFactory = factory
    }

    @JvmStatic
    fun setTargetActivityFactory(factory: SplashIntentFactory) {
        targetActivityFactory = factory
    }

    @JvmStatic
    fun setFallbackActivityFactory(factory: SplashIntentFactory) {
        fallbackActivityFactory = factory
    }

    @JvmStatic
    fun setDefaultHomeFallbackActivityFactory(factory: SplashIntentFactory) {
        defaultHomeFallbackActivityFactory = factory
    }

    @JvmStatic
    fun resetFragmentFactory() {
        fragmentFactory = SplashFragmentFactory { DefaultSplashFragment() }
    }

    @JvmStatic
    fun resetNavigationFactories() {
        targetActivityFactory = null
        fallbackActivityFactory = null
        defaultHomeFallbackActivityFactory = null
    }

    internal fun createFragment(): Fragment = fragmentFactory.createFragment()

    internal fun createTargetActivityIntent(context: Context): Intent =
        targetActivityFactory?.createIntent(context)
            ?: error("Missing splash target activity factory")

    internal fun createFallbackActivityIntent(context: Context): Intent =
        fallbackActivityFactory?.createIntent(context)
            ?: error("Missing splash fallback activity factory")

    internal fun createDefaultHomeFallbackActivityIntent(context: Context): Intent =
        defaultHomeFallbackActivityFactory?.createIntent(context)
            ?: error("Missing splash default-home fallback activity factory")
}
