package app.lawnchair.bi.a

import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.android.launcher3.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * AdSlotProvider implementation for news feed using AdMob banner ads.
 * Each ad slot gets its own AdView instance that loads independently.
 */
class NewsAdSlotProvider {

    companion object {
        private const val TAG = "adm.news"
        private const val PLACEMENT_ID = "2001"
    }

    fun isEnabled(): Boolean {
        return BuildConfig.AD_ENABLED
    }

    fun createAdView(parent: ViewGroup): View? {
        if (!isEnabled()) return null

        val metadata = AdConfigurationManager.createMetadata(PLACEMENT_ID) ?: return null
        val unitId = metadata.getPrimaryUnitId()
        if (unitId.isEmpty()) return null

        val adView = AdView(parent.context).apply {
            setAdSize(AdSize.MEDIUM_RECTANGLE)
            adUnitId = unitId
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.d(TAG, "News feed ad loaded")
                    visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "News feed ad load failed: ${error.message}")
                    visibility = View.GONE
                }
            }
        }

        adView.loadAd(AdRequest.Builder().build())
        return adView
    }

    fun onAdViewRecycled(view: View) {
        // Find and destroy AdView when recycled
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (child is AdView) {
                    child.destroy()
                }
            }
        }
    }
}
