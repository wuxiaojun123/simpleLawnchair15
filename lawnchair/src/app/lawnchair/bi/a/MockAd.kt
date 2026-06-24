package app.lawnchair.bi.a

import android.content.Context

/**
 * Mock ad implementation for testing
 * 
 * created on 2025/8/10
 * @author holmes
 */
class MockAd(
    metadata: AdMetadata
) : AbstractAd(metadata) {
    
    override fun getPlatform(): String = "Mock"
    
    override fun loadAd(context: Context) {
        // Simulate loading delay
        handler.postDelayed({
            if (state == AdState.LOADING) {
                materialEcpm = (10..100).random().toDouble()
                onLoadSuccess()
            }
        }, (500..2000).random().toLong())
    }
    
    override fun showAd(context: Context, container: AdContainer?) {
        // Simulate show success
        handler.post {
            if (state == AdState.SHOWING) {
                onShowSuccess()
            }
        }
    }
    
    override fun destroyAd() {
        handler.removeCallbacksAndMessages(null)
    }
}
