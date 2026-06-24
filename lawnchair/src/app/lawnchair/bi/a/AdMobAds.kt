package app.lawnchair.bi.a

import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdMob ad implementations
 * 
 * created on 2025/8/10
 * @author holmes
 */

private const val TAG = "adm.admob"

// === Banner ===
class AdMobBannerAd(metadata: AdMetadata) : AbstractAd(metadata) {

    private var adView: AdView? = null

    override fun getPlatform(): String = AdPlatforms.ADMOB

    override fun loadAd(context: Context) {
        val unitId = metadata.getPrimaryUnitId()
        if (unitId.isEmpty()) {
            onLoadFailed(AdErrorC(AdErrorC.ERROR_INVALID_REQUEST, "No unit ID"))
            return
        }

        adView = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = unitId
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "Banner loaded: $unitId")
                    onLoadSuccess()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Banner load failed: ${error.message}")
                    onLoadFailed(AdErrorC(error.code.toString(), error.message))
                }

                override fun onAdClicked() {
                    onClicked()
                }

                override fun onAdClosed() {
                    onClosed()
                }

                override fun onAdImpression() {
                    onShowSuccess()
                }
            }

            onPaidEventListener = OnPaidEventListener { adValue ->
                extractRevenue(adValue, unitId)
            }
        }

        adView?.loadAd(AdRequest.Builder().build())
    }

    override fun showAd(context: Context, container: AdContainer?) {
        val parent = container?.getParent()
        if (parent != null && adView != null) {
            (adView?.parent as? FrameLayout)?.removeView(adView)
            parent.addView(adView)
        }
        // Banner shows immediately on load, so mark as shown
        onShowSuccess()
    }

    override fun destroyAd() {
        adView?.destroy()
        adView = null
    }

    private fun extractRevenue(adValue: AdValue, unitId: String) {
        val revenue = AdRevenue(
            plat = AdPlatforms.ADMOB,
            network = "admob",
            adType = AdType.BANNER,
            unitId = unitId,
            value = adValue.valueMicros / 1_000_000.0,
            currency = adValue.currencyCode
        )
        setRevenueInfo(revenue)
        materialEcpm = revenue.value * 1000
    }
}

// === Interstitial ===
class AdMobInterstitialAd(metadata: AdMetadata) : AbstractAd(metadata) {

    private var interstitialAd: InterstitialAd? = null

    override fun getPlatform(): String = AdPlatforms.ADMOB

    override fun loadAd(context: Context) {
        val unitId = metadata.getPrimaryUnitId()
        if (unitId.isEmpty()) {
            onLoadFailed(AdErrorC(AdErrorC.ERROR_INVALID_REQUEST, "No unit ID"))
            return
        }

        InterstitialAd.load(
            context,
            unitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial loaded: $unitId")
                    interstitialAd = ad
                    setupCallbacks(ad, unitId)
                    onLoadSuccess()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial load failed: ${error.message}")
                    onLoadFailed(AdErrorC(error.code.toString(), error.message))
                }
            }
        )
    }

    private fun setupCallbacks(ad: InterstitialAd, unitId: String) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                onShowSuccess()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                onShowFailed(AdErrorC(error.code.toString(), error.message))
            }

            override fun onAdDismissedFullScreenContent() {
                onClosed()
                interstitialAd = null
            }

            override fun onAdClicked() {
                onClicked()
            }

            override fun onAdImpression() {}
        }

        ad.onPaidEventListener = OnPaidEventListener { adValue ->
            val revenue = AdRevenue(
                plat = AdPlatforms.ADMOB,
                network = "admob",
                adType = AdType.INTERSTITIAL,
                unitId = unitId,
                value = adValue.valueMicros / 1_000_000.0,
                currency = adValue.currencyCode
            )
            setRevenueInfo(revenue)
            materialEcpm = revenue.value * 1000
        }
    }

    override fun showAd(context: Context, container: AdContainer?) {
        val activity = container?.getActivity() ?: ContextActivityContainer(context).getActivity()
        if (activity == null) {
            onShowFailed(AdErrorC(AdErrorC.ERROR_LIFECYCLE, "No activity context"))
            return
        }
        interstitialAd?.show(activity)
    }

    override fun destroyAd() {
        interstitialAd = null
    }
}

// === Native ===
class AdMobNativeAd(metadata: AdMetadata) : AbstractAd(metadata) {

    private var nativeAd: NativeAd? = null

    override fun getPlatform(): String = AdPlatforms.ADMOB

    override fun loadAd(context: Context) {
        val unitId = metadata.getPrimaryUnitId()
        if (unitId.isEmpty()) {
            onLoadFailed(AdErrorC(AdErrorC.ERROR_INVALID_REQUEST, "No unit ID"))
            return
        }

        val adLoader = com.google.android.gms.ads.AdLoader.Builder(context, unitId)
            .forNativeAd { ad ->
                Log.d(TAG, "Native ad loaded: $unitId")
                nativeAd = ad
                ad.setOnPaidEventListener { adValue ->
                    val revenue = AdRevenue(
                        plat = AdPlatforms.ADMOB,
                        network = "admob",
                        adType = AdType.NATIVE,
                        unitId = unitId,
                        value = adValue.valueMicros / 1_000_000.0,
                        currency = adValue.currencyCode
                    )
                    setRevenueInfo(revenue)
                    materialEcpm = revenue.value * 1000
                }
                onLoadSuccess()
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Native load failed: ${error.message}")
                    onLoadFailed(AdErrorC(error.code.toString(), error.message))
                }

                override fun onAdClicked() {
                    onClicked()
                }

                override fun onAdImpression() {
                    onShowSuccess()
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    override fun showAd(context: Context, container: AdContainer?) {
        val ad = nativeAd
        if (ad == null) {
            onShowFailed(AdErrorC(AdErrorC.ERROR_NOT_READY, "Native ad not loaded"))
            return
        }

        // For native ads, the rendering is done externally via INativeRender
        // Just mark as shown
        onShowSuccess()
    }

    fun getNativeAd(): NativeAd? = nativeAd

    override fun destroyAd() {
        nativeAd?.destroy()
        nativeAd = null
    }
}

// === Rewarded ===
class AdMobRewardedAd(metadata: AdMetadata) : AbstractAd(metadata) {

    private var rewardedAd: RewardedAd? = null

    override fun getPlatform(): String = AdPlatforms.ADMOB

    override fun loadAd(context: Context) {
        val unitId = metadata.getPrimaryUnitId()
        if (unitId.isEmpty()) {
            onLoadFailed(AdErrorC(AdErrorC.ERROR_INVALID_REQUEST, "No unit ID"))
            return
        }

        RewardedAd.load(
            context,
            unitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded loaded: $unitId")
                    rewardedAd = ad
                    setupCallbacks(ad, unitId)
                    onLoadSuccess()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded load failed: ${error.message}")
                    onLoadFailed(AdErrorC(error.code.toString(), error.message))
                }
            }
        )
    }

    private fun setupCallbacks(ad: RewardedAd, unitId: String) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                onShowSuccess()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                onShowFailed(AdErrorC(error.code.toString(), error.message))
            }

            override fun onAdDismissedFullScreenContent() {
                onClosed()
                rewardedAd = null
            }

            override fun onAdClicked() {
                onClicked()
            }
        }

        ad.onPaidEventListener = OnPaidEventListener { adValue ->
            val revenue = AdRevenue(
                plat = AdPlatforms.ADMOB,
                network = "admob",
                adType = AdType.REWARDED,
                unitId = unitId,
                value = adValue.valueMicros / 1_000_000.0,
                currency = adValue.currencyCode
            )
            setRevenueInfo(revenue)
            materialEcpm = revenue.value * 1000
        }
    }

    override fun showAd(context: Context, container: AdContainer?) {
        val activity = container?.getActivity() ?: ContextActivityContainer(context).getActivity()
        if (activity == null) {
            onShowFailed(AdErrorC(AdErrorC.ERROR_LIFECYCLE, "No activity context"))
            return
        }
        rewardedAd?.show(activity) { rewardItem ->
            Log.d(TAG, "User rewarded: ${rewardItem.amount} ${rewardItem.type}")
            onRewarded()
        }
    }

    override fun destroyAd() {
        rewardedAd = null
    }
}

// === Splash (Interstitial-based) ===
class AdMobSplashAd(metadata: AdMetadata) : AbstractAd(metadata) {

    private var interstitialAd: InterstitialAd? = null

    override fun getPlatform(): String = AdPlatforms.ADMOB

    override fun loadAd(context: Context) {
        val unitId = metadata.getPrimaryUnitId()
        if (unitId.isEmpty()) {
            onLoadFailed(AdErrorC(AdErrorC.ERROR_INVALID_REQUEST, "No unit ID"))
            return
        }

        InterstitialAd.load(
            context,
            unitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Splash (interstitial) loaded: $unitId")
                    interstitialAd = ad
                    setupCallbacks(ad, unitId)
                    onLoadSuccess()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Splash load failed: ${error.message}")
                    onLoadFailed(AdErrorC(error.code.toString(), error.message))
                }
            }
        )
    }

    private fun setupCallbacks(ad: InterstitialAd, unitId: String) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                onShowSuccess()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                onShowFailed(AdErrorC(error.code.toString(), error.message))
            }

            override fun onAdDismissedFullScreenContent() {
                onClosed()
                interstitialAd = null
            }

            override fun onAdClicked() {
                onClicked()
            }
        }

        ad.onPaidEventListener = OnPaidEventListener { adValue ->
            val revenue = AdRevenue(
                plat = AdPlatforms.ADMOB,
                network = "admob",
                adType = AdType.SPLASH,
                unitId = unitId,
                value = adValue.valueMicros / 1_000_000.0,
                currency = adValue.currencyCode
            )
            setRevenueInfo(revenue)
            materialEcpm = revenue.value * 1000
        }
    }

    override fun showAd(context: Context, container: AdContainer?) {
        val activity = container?.getActivity() ?: ContextActivityContainer(context).getActivity()
        if (activity == null) {
            onShowFailed(AdErrorC(AdErrorC.ERROR_LIFECYCLE, "No activity context"))
            return
        }
        interstitialAd?.show(activity)
    }

    override fun destroyAd() {
        interstitialAd = null
    }
}
