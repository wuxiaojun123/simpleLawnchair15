package com.nice.library_news

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NewsPreloadManager {
    private const val PREFS_NAME = "news_preload_prefs"
    private const val KEY_LAST_PRELOAD_DATE = "last_preload_date"
    private const val INITIAL_NEWS_COUNT = 10

    @Volatile
    private var preloadInProgress = false

    fun preloadOnFirstUnlockIfNeeded(context: Context) {
        if (preloadInProgress) return
        if (!shouldPreloadToday(context)) return

        preloadInProgress = true
        markPreloadedToday(context)
        runCatching {
            FakeNewsRepository.loadInitialPage(minCount = INITIAL_NEWS_COUNT)
        }.onSuccess { result ->
            if (result.items.isNotEmpty()) {
                NewsLocalCache.save(
                    context = context,
                    items = result.items,
                    nextPage = result.nextPage,
                    hasMore = result.hasMore && result.items.isNotEmpty(),
                )
            }
        }.onFailure {
            // Keep this path silent. It is a background warm-up only.
        }
        preloadInProgress = false
    }

    private fun shouldPreloadToday(context: Context): Boolean {
        if (!NewsLocalCache.shouldRefreshToday(context)) {
            return false
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_PRELOAD_DATE, null) != todayKey()
    }

    private fun markPreloadedToday(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_PRELOAD_DATE, todayKey())
            .apply()
    }

    private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
