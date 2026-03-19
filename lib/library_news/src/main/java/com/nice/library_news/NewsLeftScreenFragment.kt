package com.nice.library_news

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class NewsLeftScreenFragment : Fragment() {
    private val handler = Handler(Looper.getMainLooper())
    private val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
    private var nextPage = 1
    private var loadingMore = false
    private var hasMore = true
    private var loadMoreRequestId = 0
    private var loadMoreTimeoutRunnable: Runnable? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter
    private var cacheLoaded = false
    private var pendingFirstVisiblePosition = RecyclerView.NO_POSITION
    private var pendingFirstVisibleOffset = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val root = inflater.inflate(R.layout.fragment_news_left_screen, container, false)
        val context = root.context
        swipeRefreshLayout = checkNotNull(root.findViewById(R.id.swipeRefreshLayout))
        recyclerView = checkNotNull(root.findViewById(R.id.newsRecyclerView))

        swipeRefreshLayout.setColorSchemeColors(
            android.graphics.Color.parseColor("#C96B12"),
            android.graphics.Color.parseColor("#F2A65A"),
            android.graphics.Color.parseColor("#6C9A8B"),
        )

        adapter = NewsAdapter(mutableListOf()) { item ->
            if (item.url.isBlank()) {
                Toast.makeText(context, item.title, Toast.LENGTH_SHORT).show()
            } else {
                startActivity(
                    Intent(context, NewsWebViewActivity::class.java)
                        .putExtra(NewsWebViewActivity.EXTRA_URL, item.url)
                        .putExtra(NewsWebViewActivity.EXTRA_TITLE, item.title),
                )
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = this@NewsLeftScreenFragment.adapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0 || loadingMore || !hasMore) return
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val totalCount = this@NewsLeftScreenFragment.adapter.itemCount
                val canScrollMore = recyclerView.canScrollVertically(1)
                if (!canScrollMore && lastVisible == totalCount - 1) {
                    loadMore()
                }
            }
        })

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = true
            refreshNews(forceRefresh = true)
        }

        val restoredFromSession = restoreSessionNews()
        if (!restoredFromSession) {
            restoreCachedNews()
        }

        if (!cacheLoaded) {
            swipeRefreshLayout.isRefreshing = true
            refreshNews(forceRefresh = true)
        } else if (!restoredFromSession && shouldRefreshInBackground()) {
            refreshNews(forceRefresh = false)
        }
        return root
    }

    private fun restoreSessionNews(): Boolean {
        val session = NewsSessionState.load() ?: return false
        if (session.items.isEmpty()) return false
        adapter.replaceAll(session.items)
        nextPage = session.nextPage
        hasMore = session.hasMore
        cacheLoaded = true
        pendingFirstVisiblePosition = session.firstVisiblePosition
        pendingFirstVisibleOffset = session.firstVisibleOffset
        restoreListStateIfNeeded()
        return true
    }

    private fun restoreCachedNews() {
        val context = activity ?: return
        val cached = NewsLocalCache.load(context) ?: return
        if (cached.items.isEmpty()) return
        adapter.replaceAll(cached.items)
        nextPage = cached.nextPage
        hasMore = cached.hasMore
        cacheLoaded = true
    }

    private fun shouldRefreshInBackground(): Boolean {
        val context = activity ?: return false
        return NewsLocalCache.shouldRefreshToday(context)
    }

    private fun refreshNews(forceRefresh: Boolean) {
        hasMore = true
        executor.execute {
            runCatching {
                FakeNewsRepository.loadInitialPage(minCount = 10)
            }.onSuccess { result ->
                handler.post {
                    adapter.replaceAll(result.items)
                    nextPage = result.nextPage
                    hasMore = result.hasMore && result.items.isNotEmpty()
                    swipeRefreshLayout.isRefreshing = false
                    cacheLoaded = result.items.isNotEmpty()
                    pendingFirstVisiblePosition = RecyclerView.NO_POSITION
                    pendingFirstVisibleOffset = 0
                    activity?.let {
                        NewsLocalCache.save(
                            context = it,
                            items = result.items,
                            nextPage = nextPage,
                            hasMore = hasMore,
                        )
                    }
                    syncSessionState()
                }
            }.onFailure { error ->
                handler.post {
                    swipeRefreshLayout.isRefreshing = false
                    if (forceRefresh || !cacheLoaded) activity?.let {
                        Toast.makeText(it, error.message ?: "Failed to load news", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadMore() {
        val requestId = ++loadMoreRequestId
        loadingMore = true
        showLoadMoreDialog()
        scheduleLoadMoreTimeout(requestId)
        val startPage = nextPage
        executor.execute {
            runCatching {
                FakeNewsRepository.loadMorePage(startPage = startPage, minCount = FakeNewsRepository.PAGE_SIZE)
            }.onSuccess { more ->
                cancelLoadMoreTimeout()
                handler.post {
                    if (requestId != loadMoreRequestId || !loadingMore) return@post
                    dismissLoadMoreDialog()
                    if (more.items.isEmpty()) {
                        hasMore = false
                    } else {
                        nextPage = more.nextPage
                        hasMore = more.hasMore
                        adapter.append(more.items)
                        activity?.let {
                            NewsLocalCache.save(
                                context = it,
                                items = adapter.snapshot(),
                                nextPage = nextPage,
                                hasMore = hasMore,
                            )
                        }
                        syncSessionState()
                    }
                    loadingMore = false
                }
            }.onFailure { error ->
                cancelLoadMoreTimeout()
                handler.post {
                    if (requestId != loadMoreRequestId || !loadingMore) return@post
                    dismissLoadMoreDialog()
                    loadingMore = false
                    activity?.let {
                        Toast.makeText(it, error.message ?: "Failed to load more", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        cancelLoadMoreTimeout()
        dismissLoadMoreDialog()
        executor.shutdownNow()
        super.onDestroy()
    }

    override fun onPause() {
        syncSessionState()
        super.onPause()
    }

    private fun scheduleLoadMoreTimeout(requestId: Int) {
        cancelLoadMoreTimeout()
        loadMoreTimeoutRunnable = Runnable {
            if (requestId != loadMoreRequestId || !loadingMore) return@Runnable
            loadingMore = false
            loadMoreRequestId += 1
            dismissLoadMoreDialog()
            activity?.let {
                Toast.makeText(it, "Load more timed out. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
        handler.postDelayed(checkNotNull(loadMoreTimeoutRunnable), LOAD_MORE_TIMEOUT_MS)
    }

    private fun cancelLoadMoreTimeout() {
        loadMoreTimeoutRunnable?.let(handler::removeCallbacks)
        loadMoreTimeoutRunnable = null
    }

    private fun showLoadMoreDialog() {
        val fragmentManager = fragmentManager ?: return
        if (fragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) != null) return
        LoadingDialogFragment().show(fragmentManager, LoadingDialogFragment.TAG)
    }

    private fun dismissLoadMoreDialog() {
        val fragmentManager = fragmentManager ?: return
        (fragmentManager.findFragmentByTag(LoadingDialogFragment.TAG) as? LoadingDialogFragment)
            ?.dismissAllowingStateLoss()
    }

    private fun restoreListStateIfNeeded() {
        val position = pendingFirstVisiblePosition
        if (position == RecyclerView.NO_POSITION) return
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        layoutManager.scrollToPositionWithOffset(position, pendingFirstVisibleOffset)
        pendingFirstVisiblePosition = RecyclerView.NO_POSITION
        pendingFirstVisibleOffset = 0
    }

    private fun syncSessionState() {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
        val firstVisiblePosition = layoutManager?.findFirstVisibleItemPosition() ?: RecyclerView.NO_POSITION
        val firstVisibleView = if (firstVisiblePosition != RecyclerView.NO_POSITION) {
            layoutManager?.findViewByPosition(firstVisiblePosition)
        } else {
            null
        }
        val firstVisibleOffset = firstVisibleView?.top ?: 0
        NewsSessionState.save(
            items = adapter.snapshot(),
            nextPage = nextPage,
            hasMore = hasMore,
            firstVisiblePosition = firstVisiblePosition,
            firstVisibleOffset = firstVisibleOffset,
        )
    }

    companion object {
        private const val LOAD_MORE_TIMEOUT_MS = 12_000L
    }
}
