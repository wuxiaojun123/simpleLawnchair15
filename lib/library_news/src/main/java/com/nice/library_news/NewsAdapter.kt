package com.nice.library_news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(
            itemView = itemView,
            image = checkNotNull(itemView.findViewById<ImageView>(R.id.newsImage)),
            title = checkNotNull(itemView.findViewById<TextView>(R.id.newsTitle)),
            summary = checkNotNull(itemView.findViewById<TextView>(R.id.newsSummary)),
            footer = checkNotNull(itemView.findViewById<TextView>(R.id.newsFooter)),
        )
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = items[position]
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

    override fun getItemCount(): Int = items.size

    fun replaceAll(newItems: List<NewsItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun append(newItems: List<NewsItem>) {
        val start = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(start, newItems.size)
    }

    fun snapshot(): List<NewsItem> = items.toList()

    class NewsViewHolder(
        itemView: View,
        val image: ImageView,
        val title: TextView,
        val summary: TextView,
        val footer: TextView,
    ) : RecyclerView.ViewHolder(itemView)

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
        private const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0 Mobile Safari/537.36"
    }
}
