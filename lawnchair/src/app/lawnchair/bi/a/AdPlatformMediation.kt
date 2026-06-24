package app.lawnchair.bi.a

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Abstract base for platform-specific ad implementations
 * 
 * created on 2025/8/10
 * @author holmes
 */
abstract class AdPlatformMediation {

    protected val TAG: String = "adm"

    protected val initCall = AtomicBoolean(false)
    
    private val _initState = MutableStateFlow(false)
    val initState: StateFlow<Boolean> = _initState

    private var _initedSuccess: Boolean = false

    abstract fun getPlatformType(): String

    abstract fun init(
        context: Context,
        appId: String,
        appKey: String,
        extras: Map<String, String>
    )

    fun isInitedSuccess(): Boolean = _initedSuccess

    protected fun onInitSuccess() {
        _initedSuccess = true
        _initState.value = true
        Log.d(TAG, "${getPlatformType()} initialized successfully")
    }

    protected fun onInitFailed(msg: String) {
        _initedSuccess = false
        _initState.value = false
        Log.e(TAG, "${getPlatformType()} init failed: $msg")
    }

    open fun updateExtras(extras: Map<String, String>) {}

    open suspend fun beforeLoadAd(context: Context): AdErrorC? {
        return null
    }

    // Override these to create platform-specific ad instances
    open fun createBanner(metadata: AdMetadata): AdBase? = null
    open fun createInterstitial(metadata: AdMetadata): AdBase? = null
    open fun createNative(metadata: AdMetadata): AdBase? = null
    open fun createRewarded(metadata: AdMetadata): AdBase? = null
    open fun createSplash(metadata: AdMetadata): AdBase? = null

    /**
     * Create an ad based on type
     */
    fun createAd(metadata: AdMetadata): AdBase? {
        return when (metadata.placement.type) {
            AdType.BANNER -> createBanner(metadata)
            AdType.INTERSTITIAL -> createInterstitial(metadata)
            AdType.NATIVE -> createNative(metadata)
            AdType.REWARDED -> createRewarded(metadata)
            AdType.SPLASH -> createSplash(metadata)
            else -> null
        }
    }

    open fun dumpRevenue(ad: AdBase): Any? = ad.getRevenue()

    open fun getPlatformConfig(): Map<String, Any> = emptyMap()
}

/**
 * Mock platform for testing
 */
object MockPlatformMediation : AdPlatformMediation() {

    override fun getPlatformType(): String = AdPlatforms.MOCK

    override fun init(
        context: Context,
        appId: String,
        appKey: String,
        extras: Map<String, String>
    ) {
        if (!initCall.compareAndSet(false, true)) {
            return
        }
        onInitSuccess()
    }

    override fun createBanner(metadata: AdMetadata): AdBase? = MockAd(metadata)
    override fun createInterstitial(metadata: AdMetadata): AdBase? = MockAd(metadata)
    override fun createNative(metadata: AdMetadata): AdBase? = MockAd(metadata)
    override fun createRewarded(metadata: AdMetadata): AdBase? = MockAd(metadata)
    override fun createSplash(metadata: AdMetadata): AdBase? = MockAd(metadata)

    override fun getPlatformConfig(): Map<String, Any> {
        return mapOf(
            "platform" to "Mock",
            "initialized" to isInitedSuccess()
        )
    }
}
