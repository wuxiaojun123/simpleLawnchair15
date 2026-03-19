package com.nice.library_news

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject

object FakeNewsRepository {
    private const val BASE_URL = "https://api.thenewsapi.com/v1/news/top"
    const val PAGE_SIZE = 12
    private const val MAX_BATCH_PAGES = 6

    data class NewsPageResult(
        val items: List<NewsItem>,
        val nextPage: Int,
        val hasMore: Boolean,
    )

    fun loadPage(page: Int): List<NewsItem> {
        val query = listOf(
            "api_token" to BuildConfig.THE_NEWS_API_TOKEN,
            "language" to "en",
            "categories" to "general,entertainment",
            "limit" to PAGE_SIZE.toString(),
            "page" to page.toString(),
        ).joinToString("&") { (key, value) ->
            "${URLEncoder.encode(key, "UTF-8")}=${URLEncoder.encode(value, "UTF-8")}"
        }

        val connection = URL("$BASE_URL?$query").openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.doInput = true

            val code = connection.responseCode
            val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use { reader -> reader.readText() }
                .orEmpty()

            if (code !in 200..299) {
                error("The News API request failed. code=$code body=$body")
            }
            parseNews(body)
        } finally {
            connection.disconnect()
        }
    }

    fun loadInitialPage(minCount: Int = 10): NewsPageResult {
        return loadBatch(startPage = 1, minCount = minCount)
    }

    fun loadMorePage(startPage: Int, minCount: Int = PAGE_SIZE): NewsPageResult {
        return loadBatch(startPage = startPage, minCount = minCount)
    }

    private fun loadBatch(startPage: Int, minCount: Int): NewsPageResult {
        val items = mutableListOf<NewsItem>()
        var page = startPage
        var hasMore = true
        var fetchedPages = 0

        while (items.size < minCount && hasMore && fetchedPages < MAX_BATCH_PAGES) {
            val pageItems = loadPage(page)
            fetchedPages += 1
            if (pageItems.isEmpty()) {
                hasMore = false
            } else {
                items += pageItems
                page += 1
            }
        }

        return NewsPageResult(
            items = items.take(minCount),
            nextPage = page,
            hasMore = hasMore,
        )
    }

    private fun parseNews(body: String): List<NewsItem> {
        val data = JSONObject(body).optJSONArray("data") ?: return emptyList()
        return buildList(data.length()) {
            for (index in 0 until data.length()) {
                val item = data.optJSONObject(index) ?: continue
                val uuid = item.optString("uuid").ifBlank { "news_$index" }
                val title = item.optString("title").ifBlank { continue }
                val description = item.optString("description").ifBlank {
                    item.optString("snippet").ifBlank { "No summary available." }
                }
                val source = item.optString("source").ifBlank { "Unknown source" }
                val publishedAt = item.optString("published_at").ifBlank { item.optString("publishedAt") }
                val imageUrl = item.optString("image_url").ifBlank { item.optString("imageUrl") }
                val articleUrl = item.optString("url")
                add(
                    NewsItem(
                        id = uuid,
                        title = title,
                        summary = description,
                        source = source,
                        publishTime = publishedAt.ifBlank { "Unknown time" },
                        imageUrl = imageUrl,
                        url = articleUrl,
                    ),
                )
            }
        }
    }
}
