package com.nice.library_news

data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val publishTime: String,
    val imageUrl: String,
    val url: String,
)
