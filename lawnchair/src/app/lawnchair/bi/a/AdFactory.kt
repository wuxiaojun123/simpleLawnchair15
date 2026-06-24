package app.lawnchair.bi.a

import android.util.Log

/**
 * Factory for creating ad instances
 * 
 * created on 2025/8/10
 * @author holmes
 */
object AdFactory {
    private const val TAG = "adm"

    /**
     * Create an ad instance based on metadata
     */
    fun createAd(metadata: AdMetadata): AdBase? {
        return AdPlatformManager.createAd(metadata)
    }

    /**
     * Check if ad platform is supported
     */
    fun isPlatformSupported(platformName: String): Boolean {
        return AdPlatformManager.getPlatform(platformName) != null
    }
}
