package app.lawnchair.bi.a

import android.util.Log
import org.json.JSONObject

/**
 * Parses ad configuration from JSON
 * 
 * created on 2025/8/10
 * @author holmes
 */
object AdConfigParser {
    private const val TAG = "adm"

    fun parse(jsonString: String): AdConfiguration? {
        return try {
            val json = JSONObject(jsonString)
            
            // Parse platform
            val platformJson = json.getJSONObject("platform")
            val platform = AdPlatform(
                name = platformJson.getString("name"),
                appid = platformJson.optString("appid", ""),
                appkey = platformJson.optString("appkey", "")
            )
            
            // Parse placements
            val placementsJson = json.getJSONArray("placement")
            val placements = mutableListOf<AdPlacement>()
            for (i in 0 until placementsJson.length()) {
                val placementJson = placementsJson.getJSONObject(i)
                
                // Parse extras
                val extras = mutableMapOf<String, Any>()
                val extrasJson = placementJson.optJSONObject("extras")
                if (extrasJson != null) {
                    val keys = extrasJson.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        extras[key] = extrasJson.get(key)
                    }
                }
                
                // Parse units
                val units = mutableListOf<AdUnit>()
                val unitsJson = placementJson.optJSONArray("units")
                if (unitsJson != null) {
                    for (j in 0 until unitsJson.length()) {
                        val unitJson = unitsJson.getJSONObject(j)
                        units.add(AdUnit(uid = unitJson.getString("uid")))
                    }
                }
                
                placements.add(AdPlacement(
                    id = placementJson.getString("id"),
                    type = placementJson.getString("type"),
                    extras = extras,
                    units = units
                ))
            }
            
            AdConfiguration(platform = platform, placement = placements)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse ad configuration", e)
            null
        }
    }
    
    /**
     * Create a sample configuration for testing
     */
    fun createSampleConfiguration(): AdConfiguration {
        return AdConfiguration(
            platform = AdPlatform(name = AdPlatforms.MOCK),
            placement = listOf(
                AdPlacement(
                    id = "test_banner",
                    type = AdType.BANNER,
                    units = listOf(AdUnit(uid = "mock_banner_1"))
                ),
                AdPlacement(
                    id = "test_interstitial",
                    type = AdType.INTERSTITIAL,
                    units = listOf(AdUnit(uid = "mock_interstitial_1"))
                ),
                AdPlacement(
                    id = "test_native",
                    type = AdType.NATIVE,
                    units = listOf(AdUnit(uid = "mock_native_1"))
                )
            )
        )
    }
}
