package app.lawnchair.bi.analyzer

import android.util.Log
import app.lawnchair.bi.a.AdBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local AD analyzer to track impression counts and eCPM by ad type
 *
 * created on 2025/12/28
 * @author holmes
 */
object AdAnalyzer {
    private const val TAG = "adan"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _dailyStats = MutableStateFlow<Map<String, AdTypeStats>>(emptyMap())
    val dailyStats: StateFlow<Map<String, AdTypeStats>> = _dailyStats.asStateFlow()

    private val _totalStats = MutableStateFlow<Map<String, AdTypeStats>>(emptyMap())
    val totalStats: StateFlow<Map<String, AdTypeStats>> = _totalStats.asStateFlow()

    private var isInitialized = false

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun getCurrentDate(): String {
        return dateFormat.format(Date())
    }

    /**
     * Initialize the analyzer with existing data
     */
    @Synchronized
    fun initialize() {
        if (isInitialized) {
            return
        }
        isInitialized = true

        scope.launch {
            // Initialize default stats if not exists
            AdAnalyticsDs.initializeDefaultStats()

            // Check and reset daily stats if needed
            checkAndResetDailyStats()

            // Load existing stats
            AdAnalyticsDs.getAllStats().collect { statsMap ->
                _dailyStats.value = statsMap
                _totalStats.value = statsMap
                isInitialized = true

                Log.d(TAG, getSummaryReport())
            }
        }
    }

    /**
     * Record an ad impression for analytics
     * @param ad The ad that was shown
     */
    fun recordImpression(ad: AdBase) {
        if (!isInitialized) {
            initialize()
        }

        scope.launch {
            val adType = ad.getAdType()
            val ecpm = ad.getEcpm()
            val revenue = ad.getRevenue()?.value ?: (ecpm / 1000.0)

            // Get current stats and record the impression
            AdAnalyticsDs.getStats(adType).first().also { currentStats ->
                currentStats?.let { stats ->
                    // Check if daily reset is needed
                    val updatedStats = if (stats.needsReset()) {
                        stats.resetDailyStats().recordImpression(ecpm, revenue)
                    } else {
                        stats.recordImpression(ecpm, revenue)
                    }

                    // Save updated stats
                    AdAnalyticsDs.updateStats(updatedStats)
                }
            }
        }
    }

    /**
     * Get daily impression count for a specific ad type
     */
    fun getDailyImpressionCount(adType: String): Int {
        return _dailyStats.value[adType]?.dailyImpressionCount ?: 0
    }

    /**
     * Get total impression count for a specific ad type
     */
    fun getTotalImpressionCount(adType: String): Int {
        return _totalStats.value[adType]?.totalImpressionCount ?: 0
    }

    /**
     * Get accumulative daily eCPM for a specific ad type
     */
    fun getDailyEcpm(adType: String): Double {
        return _dailyStats.value[adType]?.getDailyEcpm() ?: 0.0
    }

    /**
     * Get accumulative total eCPM for a specific ad type
     */
    fun getTotalEcpm(adType: String): Double {
        return _totalStats.value[adType]?.getTotalEcpm() ?: 0.0
    }

    /**
     * Reset daily statistics for all ad types
     */
    fun resetDailyStats() {
        scope.launch {
            AdAnalyticsDs.resetDailyStats()
        }
    }

    /**
     * Check if daily stats need to be reset
     */
    private fun checkAndResetDailyStats() {
        _dailyStats.value.values.forEach { stats ->
            if (stats.needsReset()) {
                resetDailyStats()
            }
        }
    }

    /**
     * Clear all analytics data (use with caution)
     */
    fun clearAll() {
        scope.launch {
            AdAnalyticsDs.clearAll()
        }
    }

    /**
     * Get formatted summary report
     */
    fun getSummaryReport(): String {
        val sb = StringBuilder()
        sb.appendLine("=== AD Analytics Report ===")
        sb.appendLine("Date: ${getCurrentDate()}")
        sb.appendLine()

        sb.appendLine("--- Daily Statistics ---")
        AdAnalyticsDs.adTypeValues.forEach { adType ->
            val impressions = getDailyImpressionCount(adType)
            val ecpm = getDailyEcpm(adType)
            sb.appendLine("$adType: Impressions=$impressions, eCPM=${String.format("%.2f", ecpm)}")
        }

        return sb.toString()
    }
}
