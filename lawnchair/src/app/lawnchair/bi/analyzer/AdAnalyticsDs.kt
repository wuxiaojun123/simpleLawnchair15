package app.lawnchair.bi.analyzer

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import app.lawnchair.LawnchairApp
import app.lawnchair.bi.a.AdType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore for persisting AD analytics data locally
 *
 * created on 2025/12/28
 * @author holmes
 */
object AdAnalyticsDs {
    private val Context.adAnalyticsDs: DataStore<Preferences> by preferencesDataStore(name = "adan")

    val dataStore: DataStore<Preferences>
        get() = LawnchairApp.instance.adAnalyticsDs

    val adTypeValues: Array<String> = arrayOf(
        AdType.BANNER,
        AdType.INTERSTITIAL,
        AdType.NATIVE,
        AdType.REWARDED,
        AdType.SPLASH
    )

    /**
     * Get all ad type statistics
     */
    fun getAllStats(): Flow<Map<String, AdTypeStats>> {
        return dataStore.data.map { preferences ->
            buildMap {
                adTypeValues.forEach { adType ->
                    val key = AdAnalyticsKeys.getStatsKey(adType)
                    preferences[key]?.let { data ->
                        AdStatsSerializer.deserialize(data)?.let { stats ->
                            put(adType, stats)
                        }
                    }
                }
            }
        }
    }

    /**
     * Get statistics for a specific ad type
     */
    fun getStats(adType: String): Flow<AdTypeStats?> {
        return dataStore.data.map { preferences ->
            val key = AdAnalyticsKeys.getStatsKey(adType)
            preferences[key]?.let { data ->
                AdStatsSerializer.deserialize(data)
            }
        }
    }

    /**
     * Update statistics for a specific ad type
     */
    suspend fun updateStats(stats: AdTypeStats) {
        dataStore.edit { preferences ->
            val key = AdAnalyticsKeys.getStatsKey(stats.adType)
            preferences[key] = AdStatsSerializer.serialize(stats)
        }
    }

    /**
     * Initialize default stats for all ad types
     */
    suspend fun initializeDefaultStats() {
        dataStore.edit { preferences ->
            adTypeValues.forEach { adType ->
                val key = AdAnalyticsKeys.getStatsKey(adType)
                if (!preferences.contains(key)) {
                    preferences[key] = AdStatsSerializer.serialize(AdTypeStats(adType = adType))
                }
            }
        }
    }

    /**
     * Reset daily statistics for all ad types
     */
    suspend fun resetDailyStats() {
        dataStore.edit { preferences ->
            adTypeValues.forEach { adType ->
                val key = AdAnalyticsKeys.getStatsKey(adType)
                preferences[key]?.let { data ->
                    AdStatsSerializer.deserialize(data)?.resetDailyStats()?.let { resetStats ->
                        preferences[key] = AdStatsSerializer.serialize(resetStats)
                    }
                }
            }
        }
    }

    /**
     * Clear all analytics data
     */
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
