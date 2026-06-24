package app.lawnchair.bi.a

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages ad material states and instances
 * 
 * created on 2025/8/10
 * @author holmes
 */
object AdMaterialManager {
    private const val TAG = "adm"

    // Ad instances by placement ID
    private val adInstances = ConcurrentHashMap<String, AdBase>()
    
    // Ad states by placement ID
    private val adStates = ConcurrentHashMap<String, String>()
    
    // Ad metadata by placement ID
    private val adMetadata = ConcurrentHashMap<String, AdMetadata>()

    /**
     * Register a new ad material for a placement
     */
    fun registerMaterial(placementId: String, metadata: AdMetadata) {
        adMetadata[placementId] = metadata
        adStates[placementId] = AdState.IDLE
        Log.d(TAG, "Registered material: $placementId (${metadata.placement.type})")
    }

    /**
     * Set ad instance for a placement
     */
    fun setAdInstance(placementId: String, ad: AdBase?) {
        if (ad != null) {
            adInstances[placementId] = ad
        } else {
            adInstances.remove(placementId)
        }
    }

    /**
     * Get ad instance for a placement
     */
    fun getAdInstance(placementId: String): AdBase? {
        return adInstances[placementId]
    }

    /**
     * Set ad state for a placement
     */
    fun setAdState(placementId: String, state: String) {
        adStates[placementId] = state
    }

    /**
     * Get ad state for a placement
     */
    fun getAdState(placementId: String): String {
        return adStates[placementId] ?: AdState.IDLE
    }

    /**
     * Check if ad is ready for a placement
     */
    fun isAdReady(placementId: String): Boolean {
        val ad = adInstances[placementId] ?: return false
        return ad.isReady()
    }

    /**
     * Check if a placement is registered
     */
    fun isRegistered(placementId: String): Boolean {
        return adMetadata.containsKey(placementId)
    }

    /**
     * Get metadata for a placement
     */
    fun getMetadata(placementId: String): AdMetadata? {
        return adMetadata[placementId]
    }

    /**
     * Get all registered placement IDs
     */
    fun getAllPlacementIds(): Set<String> {
        return adMetadata.keys.toSet()
    }

    /**
     * Clear all materials
     */
    fun clearAll() {
        adInstances.values.forEach { it.destroy() }
        adInstances.clear()
        adStates.clear()
        adMetadata.clear()
        Log.d(TAG, "All materials cleared")
    }

    /**
     * Get ad stats
     */
    data class AdStats(
        val totalRegistered: Int,
        val totalLoaded: Int,
        val totalReady: Int
    )

    fun getStats(): AdStats {
        return AdStats(
            totalRegistered = adMetadata.size,
            totalLoaded = adStates.count { it.value == AdState.LOADED },
            totalReady = adInstances.count { it.value.isReady() }
        )
    }
}
