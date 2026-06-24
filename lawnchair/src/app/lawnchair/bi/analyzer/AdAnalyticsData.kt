package app.lawnchair.bi.analyzer

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import app.lawnchair.LawnchairApp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AD analytics data models for local analysis
 *
 * created on 2025/12/28
 * @author holmes
 */

/**
 * Data class to store ad statistics for a specific ad type
 */
@Serializable
data class AdTypeStats(
    @SerialName("t")
    val adType: String,
    @SerialName("dic")
    val dailyImpressionCount: Int = 0,
    @SerialName("tic")
    val totalImpressionCount: Int = 0,
    @SerialName("des")
    val dailyEcpmSum: Double = 0.0,
    @SerialName("drs")
    val dailyRevenueSum: Double = 0.0,
    @SerialName("tes")
    val totalEcpmSum: Double = 0.0,
    @SerialName("trs")
    val totalRevenueSum: Double = 0.0,
    @SerialName("ud")
    val lastUpdateDate: String = getCurrentDate()
) {

    companion object {
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        fun getCurrentDate(): String {
            return dateFormat.format(Date())
        }
    }

    /**
     * Calculate accumulative daily eCPM by ad type
     * Formula: Sum of eCPM / Number of impressions
     */
    fun getDailyEcpm(): Double {
        return if (dailyImpressionCount > 0) {
            dailyEcpmSum / dailyImpressionCount
        } else {
            0.0
        }
    }

    /**
     * Calculate accumulative total eCPM by ad type
     * Formula: Sum of eCPM / Number of impressions
     */
    fun getTotalEcpm(): Double {
        return if (totalImpressionCount > 0) {
            totalEcpmSum / totalImpressionCount
        } else {
            0.0
        }
    }

    /**
     * Check if data needs to be reset (new day)
     */
    fun needsReset(): Boolean {
        val currentDate = getCurrentDate()
        return lastUpdateDate != currentDate
    }

    /**
     * Reset daily statistics while preserving total statistics
     */
    fun resetDailyStats(): AdTypeStats {
        return copy(
            dailyImpressionCount = 0,
            dailyEcpmSum = 0.0,
            dailyRevenueSum = 0.0,
            lastUpdateDate = getCurrentDate()
        )
    }

    /**
     * Record a new impression with eCPM and revenue
     */
    fun recordImpression(ecpm: Double, revenue: Double = 0.0): AdTypeStats {
        return copy(
            dailyImpressionCount = dailyImpressionCount + 1,
            totalImpressionCount = totalImpressionCount + 1,
            dailyEcpmSum = dailyEcpmSum + ecpm,
            totalEcpmSum = totalEcpmSum + ecpm,
            dailyRevenueSum = dailyRevenueSum + revenue,
            totalRevenueSum = totalRevenueSum + revenue,
            lastUpdateDate = getCurrentDate()
        )
    }
}

/**
 * Key generator for DataStore preferences keys with caching
 */
object AdAnalyticsKeys {
    private const val PREFIX = "adan"

    private val keyCache = mutableMapOf<String, androidx.datastore.preferences.core.Preferences.Key<String>>()

    fun getStatsKey(adType: String): androidx.datastore.preferences.core.Preferences.Key<String> {
        return keyCache.getOrPut(adType) {
            stringPreferencesKey("${PREFIX}_stats_${adType}")
        }
    }

    fun getLastResetKey(): androidx.datastore.preferences.core.Preferences.Key<String> {
        return keyCache.getOrPut("last_reset") {
            stringPreferencesKey("${PREFIX}_last_reset_date")
        }
    }
}

/**
 * Serializer for AdTypeStats to/from JSON string
 */
object AdStatsSerializer {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    fun serialize(stats: AdTypeStats): String {
        return json.encodeToString(AdTypeStats.serializer(), stats)
    }

    fun deserialize(data: String): AdTypeStats? {
        return try {
            json.decodeFromString(AdTypeStats.serializer(), data)
        } catch (e: Exception) {
            null
        }
    }
}
