package app.lawnchair.bi.a

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

/**
 * AdMob platform mediation implementation
 *
 * created on 2025/8/10
 * @author holmes
 */
object AdMobPlatformMediation : AdPlatformMediation() {

    private val umpState = MutableStateFlow(UmpState.IDLE)
    private var enabledUmp = true

    @Volatile
    private var umpErrorMessage: String? = null
    @Volatile
    private var latestExtras: Map<String, String> = emptyMap()

    override fun getPlatformType(): String {
        return AdPlatforms.ADMOB
    }

    override fun init(
        context: Context,
        appId: String,
        appKey: String,
        extras: Map<String, String>
    ) {
        if (!initCall.compareAndSet(false, true)) {
            return
        }

        latestExtras = extras
        try {
            if (!enabledUmp) {
                MobileAds.initialize(context) { initStatus ->
                    Log.d(TAG, "AdMob initialized: $initStatus")
                    onInitSuccess()
                }
            } else {
                onInitSuccess() // mark inited, then ensure by beforeLoadAd
                tryGatherConsent(context)
            }
        } catch (e: Exception) {
            onInitFailed("AdMob initialization failed: ${e.message}")
        }
    }

    private fun tryGatherConsent(context: Context) {
        val processed = umpState.value.let {
            it == UmpState.READY || it == UmpState.BLOCKED
        }

        if (processed) {
            return
        }

        val activity = ContextActivityContainer(context).getActivity() ?: return
        if (umpState.value == UmpState.LOADING) {
            return
        }

        umpState.value = UmpState.LOADING
        val consentManager = GoogleMobileAdsConsentManager.getInstance(activity)
        consentManager.testDeviceId = latestExtras["test_device_id"]

        consentManager.gatherConsent(activity) { error ->
            if (error != null) {
                Log.w(TAG, "UMP consent error: ${error.message}")
                umpErrorMessage = error.message
            }
            MobileAds.initialize(activity) { initStatus ->
                Log.d(TAG, "AdMob initialized after consent: $initStatus")
            }
            if (consentManager.canRequestAds) {
                umpState.value = UmpState.READY
            } else {
                umpState.value = UmpState.BLOCKED
            }
        }
    }

    override suspend fun beforeLoadAd(context: Context): AdErrorC? {
        if (!enabledUmp) {
            return null
        }

        val processed = umpState.value.let {
            it == UmpState.READY || it == UmpState.BLOCKED
        }

        if (processed) {
            return null
        }

        val activity = ContextActivityContainer(context).getActivity()
        if (activity == null && umpState.value == UmpState.IDLE) {
            return AdErrorC(
                AdErrorC.ERROR_INTERNAL,
                "UMP requires an Activity context before requesting ads"
            )
        }

        tryGatherConsent(context)

        val state = withTimeoutOrNull(60_000L) {
            umpState
                .filter { it == UmpState.READY || it == UmpState.BLOCKED }
                .first()
        } ?: return AdErrorC(
            AdErrorC.ERROR_TIMEOUT,
            "UMP consent timeout"
        )

        return when (state) {
            UmpState.READY -> null
            UmpState.BLOCKED -> null
            else -> AdErrorC(
                AdErrorC.ERROR_INTERNAL,
                "UMP consent not granted"
            )
        }
    }

    override fun createBanner(metadata: AdMetadata): AdBase? {
        return AdMobBannerAd(metadata)
    }

    override fun createInterstitial(metadata: AdMetadata): AdBase? {
        return AdMobInterstitialAd(metadata)
    }

    override fun createNative(metadata: AdMetadata): AdBase? {
        return AdMobNativeAd(metadata)
    }

    override fun createRewarded(metadata: AdMetadata): AdBase? {
        return AdMobRewardedAd(metadata)
    }

    override fun createSplash(metadata: AdMetadata): AdBase? {
        return AdMobSplashAd(metadata)
    }

    override fun dumpRevenue(ad: AdBase): Any? {
        return ad.getRevenue()
    }

    override fun getPlatformConfig(): Map<String, Any> {
        return mapOf(
            "platform" to "AdMob",
            "initialized" to isInitedSuccess()
        )
    }
}
