package app.lawnchair.bi.a

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log

/**
 * Abstract ad implementation with common functionality
 * 
 * created on 2025/8/10
 * @author holmes
 */
abstract class AbstractAd(
    override val metadata: AdMetadata
) : AdBase {

    companion object {
        private const val TAG = "adm"
        const val DEFAULT_LOAD_TIMEOUT = 30_000L
        const val DEFAULT_SHOW_TIMEOUT = 10_000L
    }

    protected val handler = Handler(Looper.getMainLooper())

    override var state: String = AdState.IDLE
        protected set

    // Listeners
    protected var loadListener: AdLoadListener? = null
    protected var showListener: AdShowListener? = null

    // Metrics
    protected var materialEcpm: Double = 0.0
    private var materialRevenue: AdRevenue? = null
    private var sourceNetwork: String = ""

    // Timing
    private var loadStartTime: Long = 0L
    private var loadEndTime: Long = 0L
    private var showStartTime: Long = 0L
    private var showEndTime: Long = 0L
    private var clickedCount: Int = 0
    private var lastClickedTime: Long = 0L

    // Timeout runnables
    private var loadTimeoutRunnable: Runnable? = null
    private var showTimeoutRunnable: Runnable? = null

    override fun getAdType(): String = metadata.placement.type

    override fun getUnitCode(): String = metadata.getPrimaryUnitId()

    override fun isReady(): Boolean = state == AdState.LOADED

    override fun getSourceNetwork(): String = sourceNetwork

    override fun getPlatform(): String = metadata.platform.name

    override fun getEcpm(): Double = materialEcpm

    override fun getRevenue(): AdRevenue? = materialRevenue

    fun getLoadDuration(): Long = if (loadEndTime > loadStartTime) loadEndTime - loadStartTime else 0L
    fun getShowDuration(): Long = if (showEndTime > showStartTime) showEndTime - showStartTime else SystemClock.elapsedRealtime() - showStartTime
    fun getClickedCount(): Int = clickedCount
    fun getClickedDuration(): Long = if (lastClickedTime > showStartTime) lastClickedTime - showStartTime else 0L

    override fun load(context: Context, listener: AdLoadListener?) {
        if (state == AdState.LOADING || state == AdState.LOADED) {
            Log.w(TAG, "Ad already in state $state, skipping load")
            return
        }

        this.loadListener = listener
        state = AdState.LOADING
        loadStartTime = SystemClock.elapsedRealtime()

        // Set load timeout
        loadTimeoutRunnable = Runnable {
            if (state == AdState.LOADING) {
                val error = AdErrorC(AdErrorC.ERROR_TIMEOUT, "Load timeout")
                error.adObj = this
                onLoadFailed(error)
            }
        }
        handler.postDelayed(loadTimeoutRunnable!!, DEFAULT_LOAD_TIMEOUT)

        loadAd(context)
    }

    override fun show(context: Context, container: AdContainer?, render: INativeRender?, listener: AdShowListener?) {
        if (state != AdState.LOADED) {
            val error = AdErrorC(AdErrorC.ERROR_NOT_READY, "Ad not ready, current state: $state")
            error.adObj = this
            listener?.onAdFailedToShow(this, error)
            return
        }

        this.showListener = listener
        state = AdState.SHOWING
        showStartTime = SystemClock.elapsedRealtime()

        // Set show timeout
        showTimeoutRunnable = Runnable {
            if (state == AdState.SHOWING) {
                val error = AdErrorC(AdErrorC.ERROR_TIMEOUT, "Show timeout")
                error.adObj = this
                onShowFailed(error)
            }
        }
        handler.postDelayed(showTimeoutRunnable!!, DEFAULT_SHOW_TIMEOUT)

        showAd(context, container)
    }

    override fun destroy() {
        state = AdState.DESTROYED
        loadTimeoutRunnable?.let { handler.removeCallbacks(it) }
        showTimeoutRunnable?.let { handler.removeCallbacks(it) }
        loadListener = null
        showListener = null
        destroyAd()
    }

    // === Callbacks for subclasses ===

    protected fun onLoadSuccess() {
        loadEndTime = SystemClock.elapsedRealtime()
        loadTimeoutRunnable?.let { handler.removeCallbacks(it) }
        state = AdState.LOADED
        loadListener?.onAdLoaded(this)
    }

    protected fun onLoadFailed(error: AdErrorC) {
        loadEndTime = SystemClock.elapsedRealtime()
        loadTimeoutRunnable?.let { handler.removeCallbacks(it) }
        state = AdState.FAILED
        error.adObj = this
        loadListener?.onAdFailedToLoad(this, error)
    }

    protected fun onShowSuccess() {
        showEndTime = SystemClock.elapsedRealtime()
        showTimeoutRunnable?.let { handler.removeCallbacks(it) }
        state = AdState.SHOWN
        showListener?.onAdShown(this)
    }

    protected fun onShowFailed(error: AdErrorC) {
        showEndTime = SystemClock.elapsedRealtime()
        showTimeoutRunnable?.let { handler.removeCallbacks(it) }
        state = AdState.FAILED
        error.adObj = this
        showListener?.onAdFailedToShow(this, error)
    }

    protected fun onClicked() {
        clickedCount++
        lastClickedTime = SystemClock.elapsedRealtime()
        showListener?.onAdClicked(this)
    }

    protected fun onClosed() {
        showEndTime = SystemClock.elapsedRealtime()
        showListener?.onAdClosed(this)
    }

    protected fun onRewarded() {
        showListener?.onAdRewarded(this)
    }

    protected fun setRevenueInfo(revenue: AdRevenue?) {
        materialRevenue = revenue
    }

    protected fun setSourceNetworkName(network: String) {
        sourceNetwork = network
    }

    // === Abstract methods for subclasses ===

    protected abstract fun loadAd(context: Context)
    protected abstract fun showAd(context: Context, container: AdContainer?)
    protected abstract fun destroyAd()
}
