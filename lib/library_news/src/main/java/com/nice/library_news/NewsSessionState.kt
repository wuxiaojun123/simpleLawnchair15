package com.nice.library_news

object NewsSessionState {
    data class SessionSnapshot(
        val items: List<NewsItem>,
        val nextPage: Int,
        val hasMore: Boolean,
        val firstVisiblePosition: Int,
        val firstVisibleOffset: Int,
    )

    private var snapshot: SessionSnapshot? = null

    fun save(
        items: List<NewsItem>,
        nextPage: Int,
        hasMore: Boolean,
        firstVisiblePosition: Int,
        firstVisibleOffset: Int,
    ) {
        snapshot = SessionSnapshot(
            items = items,
            nextPage = nextPage,
            hasMore = hasMore,
            firstVisiblePosition = firstVisiblePosition,
            firstVisibleOffset = firstVisibleOffset,
        )
    }

    fun load(): SessionSnapshot? = snapshot
}
