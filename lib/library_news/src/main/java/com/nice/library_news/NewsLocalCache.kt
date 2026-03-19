package com.nice.library_news

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NewsLocalCache {
    private const val PREFS_NAME = "news_left_screen_cache"
    private const val KEY_ITEMS = "items"
    private const val KEY_NEXT_PAGE = "next_page"
    private const val KEY_HAS_MORE = "has_more"
    private const val KEY_LAST_REFRESH_DATE = "last_refresh_date"

    data class CachedNews(
        val items: List<NewsItem>,
        val nextPage: Int,
        val hasMore: Boolean,
        val lastRefreshDate: String,
    )

    fun load(context: Context): CachedNews? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val itemsJson = prefs.getString(KEY_ITEMS, null).orEmpty()
        if (itemsJson.isBlank()) return null
        val itemsArray = JSONArray(itemsJson)
        val items = buildList(itemsArray.length()) {
            for (index in 0 until itemsArray.length()) {
                val item = itemsArray.optJSONObject(index) ?: continue
                add(
                    NewsItem(
                        id = item.optString("id"),
                        title = item.optString("title"),
                        summary = item.optString("summary"),
                        source = item.optString("source"),
                        publishTime = item.optString("publishTime"),
                        imageUrl = item.optString("imageUrl"),
                        url = item.optString("url"),
                    ),
                )
            }
        }
        return CachedNews(
            items = items,
            nextPage = prefs.getInt(KEY_NEXT_PAGE, 1),
            hasMore = prefs.getBoolean(KEY_HAS_MORE, true),
            lastRefreshDate = prefs.getString(KEY_LAST_REFRESH_DATE, "").orEmpty(),
        )
    }

    fun save(context: Context, items: List<NewsItem>, nextPage: Int, hasMore: Boolean) {
        val jsonArray = JSONArray()
        items.forEach { item ->
            jsonArray.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("summary", item.summary)
                    put("source", item.source)
                    put("publishTime", item.publishTime)
                    put("imageUrl", item.imageUrl)
                    put("url", item.url)
                },
            )
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ITEMS, jsonArray.toString())
            .putInt(KEY_NEXT_PAGE, nextPage)
            .putBoolean(KEY_HAS_MORE, hasMore)
            .putString(KEY_LAST_REFRESH_DATE, todayKey())
            .apply()
    }

    fun shouldRefreshToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_REFRESH_DATE, null) != todayKey()
    }

    private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
