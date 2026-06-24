package com.nice.library_news

import android.view.View
import android.view.ViewGroup

/**
 * Interface for injecting ad views into the news feed.
 * The main app implements this to provide ad loading without
 * the news module depending on any ad SDK.
 */
interface AdSlotProvider {
    /**
     * Whether ad slots should be shown
     */
    fun isEnabled(): Boolean

    /**
     * Create and populate an ad view for the given container.
     * The implementation should load and render the ad into the returned view.
     * Return null if no ad is available.
     */
    fun createAdView(parent: ViewGroup): View?

    /**
     * Called when an ad slot view is recycled / detached.
     */
    fun onAdViewRecycled(view: View) {}
}
