package com.nice.library_news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class NewsAdapter(
    private val items: MutableList<NewsItem>,
    private val onItemClick: (NewsItem) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var adSlotProvider: AdSlotProvider? = null

    override fun getItemViewType(position: Int): Int {
        return if (isAdPosition(position)) VIEW_TYPE_AD else VIEW_TYPE_NEWS
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_AD) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_ad_slot, parent, false)
            AdViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
            NewsViewHolder(
                itemView = itemView,
                image = checkNotNull(itemView.findViewById<ImageView>(R.id.newsImage)),
                title = checkNotNull(itemView.findViewById<TextView>(R.id.newsTitle)),
                summary = checkNotNull(itemView.findViewById<TextView>(R.id.newsSummary)),
                footer = checkNotNull(itemView.findViewById<TextView>(R.id.newsFooter)),
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AdViewHolder) {
            bindAdViewHolder(holder)
        } else if (holder is NewsViewHolder) {
            val newsIndex = toNewsIndex(position)
            if (newsIndex in items.indices) {
                val item = items[newsIndex]
                Glide.with(holder.image)
                    .load(buildImageModel(item))
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.image)
                holder.title.text = item.title
                holder.summary.text = item.summary
                holder.footer.text = "Published ${item.publishTime}"
                holder.itemView.setOnClickListener { onItemClick(item) }
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is AdViewHolder) {
            adSlotProvider?.onAdViewRecycled(holder.itemView)
        }
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int {
        val newsCount = items.size
        if (!isAdEnabled() || newsCount == 0) return newsCount
        return newsCount + getAdCount(newsCount)
    }

    fun replaceAll(newItems: List<NewsItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun append(newItems: List<NewsItem>) {
        val oldTotal = itemCount
        items.addAll(newItems)
        val newTotal = itemCount
        notifyItemRangeInserted(oldTotal, newTotal - oldTotal)
    }

    fun snapshot(): List<NewsItem> = items.toList()

    private fun isAdEnabled(): Boolean = adSlotProvider?.isEnabled() == true

    private fun isAdPosition(position: Int): Boolean {
        if (!isAdEnabled()) return false
        return (position + 1) % (AD_INTERVAL + 1) == 0
    }

    private fun toNewsIndex(position: Int): Int {
        if (!isAdEnabled()) return position
        val adsBefore = position / (AD_INTERVAL + 1)
        return position - adsBefore
    }

    private fun getAdCount(newsCount: Int): Int {
        return newsCount / AD_INTERVAL
    }

    private fun bindAdViewHolder(holder: AdViewHolder) {
        val container = holder.itemView.findViewById<FrameLayout>(R.id.adSlotContainer)
        container!!.removeAllViews()
        val adView = adSlotProvider?.createAdView(container)
        if (adView != null) {
            container.addView(adView)
        }
    }

    class NewsViewHolder(
        itemView: View,
        val image: ImageView,
        val title: TextView,
        val summary: TextView,
        val footer: TextView,
    ) : RecyclerView.ViewHolder(itemView)

    class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private fun buildImageModel(item: NewsItem): Any? {
        val rawUrl = item.imageUrl.takeIf { it.isNotBlank() } ?: return null
        val normalizedUrl = normalizeUrl(rawUrl)
        return GlideUrl(
            normalizedUrl,
            LazyHeaders.Builder()
                .addHeader("User-Agent", DEFAULT_USER_AGENT)
                .addHeader("Referer", buildReferer(item.url, normalizedUrl))
                .build(),
        )
    }

    private fun buildReferer(articleUrl: String, imageUrl: String): String {
        val normalizedArticleUrl = normalizeUrl(articleUrl)
        if (normalizedArticleUrl.isNotBlank()) {
            return normalizedArticleUrl
        }
        val schemeEnd = imageUrl.indexOf("://")
        if (schemeEnd <= 0) return imageUrl
        val hostStart = schemeEnd + 3
        val pathStart = imageUrl.indexOf('/', hostStart)
        return if (pathStart > 0) imageUrl.substring(0, pathStart) else imageUrl
    }

    private fun normalizeUrl(url: String): String {
        if (url.isBlank()) return url
        return if (url.startsWith("http://")) {
            "https://" + url.removePrefix("http://")
        } else {
            url
        }
    }

    companion object {
        private const val VIEW_TYPE_NEWS = 0
        private const val VIEW_TYPE_AD = 1
        private const val AD_INTERVAL = 3
        private const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0 Mobile Safari/537.36"
    }
}
