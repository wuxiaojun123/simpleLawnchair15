package app.lawnchair.bi.a

import android.content.Context
import android.util.Log
import com.android.launcher3.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Main ad manager - entry point for ad operations
 * All operations are guarded by AD_ENABLED BuildConfig flag
 * 
 * created on 2025/8/10
 * @author holmes
 */
object Adm {

    private const val TAG = "adm"

    var debug = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * Whether ads are enabled (controlled by gradle.properties adEnabled)
     */
    val isAdEnabled: Boolean
        get() = BuildConfig.AD_ENABLED

    /**
     * Initialize the ad system with configuration
     */
    fun init(context: Context, configJson: String, extras: Map<String, String> = emptyMap()) {
        if (!isAdEnabled) {
            Log.d(TAG, "Ads disabled by build config")
            return
        }

        val config = AdConfigParser.parse(configJson)
        if (config == null) {
            Log.e(TAG, "Failed to parse ad config")
            return
        }

        initWithConfig(context, config, extras)
    }

    /**
     * Initialize with a pre-built configuration
     */
    fun initWithConfig(context: Context, config: AdConfiguration, extras: Map<String, String> = emptyMap()) {
        if (!isAdEnabled) {
            Log.d(TAG, "Ads disabled by build config")
            return
        }

        AdConfigurationManager.setConfiguration(config)
        AdConfigurationManager.registerAllPlacements()

        // Initialize the platform
        AdPlatformManager.initPlatform(
            context = context,
            platformName = config.platform.name,
            appId = config.platform.appid,
            appKey = config.platform.appkey,
            extras = extras
        )

        Log.d(TAG, "Ad system initialized: platform=${config.platform.name}, placements=${config.placement.size}")
    }

    /**
     * Preload ads for a placement
     */
    fun preloadAd(context: Context, placementId: String, callback: AdLoadCallback? = null) {
        if (!isAdEnabled) {
            callback?.onFailure(placementId, AdErrorC(AdErrorC.ERROR_INTERNAL, "Ads disabled"))
            return
        }

        scope.launch {
            val metadata = AdConfigurationManager.createMetadata(placementId)
            if (metadata == null) {
                callback?.onFailure(placementId, AdErrorC(AdErrorC.ERROR_INVALID_REQUEST, "Placement not found: $placementId"))
                return@launch
            }

            // Wait for platform if needed
            val mediation = AdPlatformManager.getPlatform(metadata.platform.name)
            if (mediation != null) {
                val error = mediation.beforeLoadAd(context)
                if (error != null) {
                    callback?.onFailure(placementId, error)
                    return@launch
                }
            }

            val ad = AdFactory.createAd(metadata)
            if (ad == null) {
                callback?.onFailure(placementId, AdErrorC(AdErrorC.ERROR_INTERNAL, "Failed to create ad for placement: $placementId"))
                return@launch
            }

            val wrappedLoadCallback = LogLoadCallback(AnalyseLoadCallback(callback))

            AdMaterialManager.setAdInstance(placementId, ad)
            ad.load(context, object : AdLoadListener {
                override fun onAdLoaded(ad: AdBase) {
                    AdMaterialManager.setAdState(placementId, AdState.LOADED)
                    wrappedLoadCallback.onSuccess(placementId, ad)
                }

                override fun onAdFailedToLoad(ad: AdBase, error: AdErrorC) {
                    AdMaterialManager.setAdState(placementId, AdState.FAILED)
                    wrappedLoadCallback.onFailure(placementId, error)
                }
            })
        }
    }

    /**
     * Load an ad for a placement
     */
    fun loadAd(context: Context, placementId: String, callback: AdLoadCallback? = null) {
        preloadAd(context, placementId, callback)
    }

    /**
     * Show an ad for a placement
     */
    fun showAd(
        context: Context,
        placementId: String,
        container: AdContainer? = null,
        render: INativeRender? = null,
        callback: AdShowCallback? = null
    ) {
        if (!isAdEnabled) {
            callback?.onFailure(placementId, AdErrorC(AdErrorC.ERROR_INTERNAL, "Ads disabled"))
            return
        }

        val ad = AdMaterialManager.getAdInstance(placementId)
        if (ad == null || !ad.isReady()) {
            callback?.onFailure(placementId, AdErrorC(AdErrorC.ERROR_NOT_READY, "Ad not ready for placement: $placementId"))
            return
        }

        val wrappedShowCallback = LogShowCallback(AnalyseShowCallback(callback))

        ad.show(context, container, render, object : AdShowListener {
            override fun onAdShown(ad: AdBase) {
                AdMaterialManager.setAdState(placementId, AdState.SHOWN)
                wrappedShowCallback.onSuccess(placementId, ad)
            }

            override fun onAdFailedToShow(ad: AdBase, error: AdErrorC) {
                AdMaterialManager.setAdState(placementId, AdState.FAILED)
                wrappedShowCallback.onFailure(placementId, error)
            }

            override fun onAdClicked(ad: AdBase) {
                wrappedShowCallback.onClicked(placementId, ad)
            }

            override fun onAdClosed(ad: AdBase) {
                wrappedShowCallback.onClosed(placementId, ad)
                // Auto-reload after close
                preloadAd(context, placementId, null)
            }

            override fun onAdRewarded(ad: AdBase) {
                wrappedShowCallback.onRewarded(placementId, ad)
            }
        })
    }

    /**
     * Check if an ad is ready for a placement
     */
    fun isAdReady(placementId: String): Boolean {
        if (!isAdEnabled) return false
        return AdMaterialManager.isAdReady(placementId)
    }

    /**
     * Destroy ad for a placement
     */
    fun destroyAd(placementId: String) {
        AdMaterialManager.getAdInstance(placementId)?.destroy()
        AdMaterialManager.setAdInstance(placementId, null)
        AdMaterialManager.setAdState(placementId, AdState.IDLE)
    }

    /**
     * Destroy all ads and clear state
     */
    fun destroyAll() {
        AdMaterialManager.clearAll()
        AdConfigurationManager.clearConfiguration()
    }
}
