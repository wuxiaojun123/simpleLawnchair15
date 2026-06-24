package app.lawnchair.bi.a

import android.util.Log

/**
 * Manages the current ad configuration
 * 
 * created on 2025/8/10
 * @author holmes
 */
object AdConfigurationManager {
    private const val TAG = "adm"

    @Volatile
    private var currentConfiguration: AdConfiguration? = null

    /**
     * Set the current configuration
     */
    @Synchronized
    fun setConfiguration(config: AdConfiguration) {
        currentConfiguration = config
        Log.d(TAG, "Configuration updated: ${config.platform.name}, ${config.placement.size} placements")
    }

    /**
     * Clear the current configuration
     */
    @Synchronized
    fun clearConfiguration() {
        currentConfiguration = null
        Log.d(TAG, "Configuration cleared")
    }

    /**
     * Get the current configuration
     */
    fun getConfiguration(): AdConfiguration? = currentConfiguration

    /**
     * Get the current platform
     */
    fun getPlatform(): AdPlatform? = currentConfiguration?.platform

    /**
     * Get all placements
     */
    fun getPlacements(): List<AdPlacement> = currentConfiguration?.placement ?: emptyList()

    /**
     * Get a specific placement by ID
     */
    fun getPlacement(id: String): AdPlacement? = currentConfiguration?.getPlacement(id)

    /**
     * Create ad metadata for a specific placement
     */
    fun createMetadata(placementId: String): AdMetadata? {
        val config = currentConfiguration ?: return null
        val placement = config.getPlacement(placementId) ?: return null
        return AdMetadata(placement = placement, platform = config.platform)
    }

    /**
     * Register all placements from current configuration
     */
    fun registerAllPlacements() {
        val config = currentConfiguration ?: return
        for (placement in config.placement) {
            val metadata = AdMetadata(placement = placement, platform = config.platform)
            AdMaterialManager.registerMaterial(placement.id, metadata)
        }
        Log.d(TAG, "Registered ${config.placement.size} placements")
    }

    /**
     * Check if configuration is valid
     */
    fun isValid(): Boolean {
        val config = currentConfiguration ?: return false
        return config.placement.isNotEmpty() && config.platform.name.isNotEmpty()
    }
}
