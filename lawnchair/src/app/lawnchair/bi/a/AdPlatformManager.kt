package app.lawnchair.bi.a

import android.content.Context
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages ad platform registrations and initialization
 * 
 * created on 2025/8/10
 * @author holmes
 */
object AdPlatformManager {

    private const val TAG = "adm"

    // Platform mediation implementations keyed by platform name
    private val platforms = ConcurrentHashMap<String, AdPlatformMediation>()

    interface AdPlatformInitCallback {
        fun onInitComplete(platform: String, success: Boolean)
    }

    init {
        // Register default platform mediations
        register(MockPlatformMediation)
        register(AdMobPlatformMediation)
    }

    /**
     * Register a platform mediation
     */
    fun register(mediation: AdPlatformMediation) {
        platforms[mediation.getPlatformType()] = mediation
        Log.d(TAG, "Registered platform: ${mediation.getPlatformType()}")
    }

    /**
     * Get platform mediation by name
     */
    fun getPlatform(name: String): AdPlatformMediation? {
        return platforms[name]
    }

    /**
     * Initialize a platform
     */
    fun initPlatform(
        context: Context,
        platformName: String,
        appId: String,
        appKey: String,
        extras: Map<String, String> = emptyMap(),
        callback: AdPlatformInitCallback? = null
    ) {
        val mediation = platforms[platformName]
        if (mediation == null) {
            Log.w(TAG, "Platform not registered: $platformName")
            callback?.onInitComplete(platformName, false)
            return
        }

        mediation.init(context, appId, appKey, extras)
        callback?.onInitComplete(platformName, true)
    }

    /**
     * Create an ad instance for a platform
     */
    fun createAd(metadata: AdMetadata): AdBase? {
        val mediation = platforms[metadata.platform.name]
        if (mediation == null) {
            Log.w(TAG, "Platform not registered: ${metadata.platform.name}")
            return null
        }
        return mediation.createAd(metadata)
    }

    /**
     * Check if a platform is initialized
     */
    fun isPlatformInitialized(platformName: String): Boolean {
        return platforms[platformName]?.isInitedSuccess() == true
    }

    /**
     * Get all registered platforms
     */
    fun getRegisteredPlatforms(): Collection<AdPlatformMediation> {
        return platforms.values
    }

    /**
     * Dump revenue info for an ad
     */
    fun dumpRevenue(ad: AdBase): Any? {
        val mediation = platforms[ad.getPlatform()] ?: return null
        return mediation.dumpRevenue(ad)
    }
}
