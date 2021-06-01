package com.lodestar.monews.adapters

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lodestar.monews.R
import com.lodestar.monews.model.Article
import kotlinx.android.synthetic.main.item_latest_news.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class NewsHomeAdapter(
    var context: Activity,
    var newsResponse: ArrayList<Article>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = "NewsHomeAdapter"

    private val EMPTY_VIEW_TYPE = 0
    private val LATEST_NEWS = 1

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            LATEST_NEWS -> {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.item_latest_news, viewGroup, false)
                return NewsViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.item_empty_view, viewGroup, false)
                EmptyViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d("1113__", "newsResponseSize: ${newsResponse.size}")
        return newsResponse.size
    }

    override fun getItemViewType(position: Int): Int {
//        val recyclerVewItem = newsResponse[position]
        return when {
            newsResponse.isNotEmpty() -> {
                LATEST_NEWS
            }
            else -> {
                EMPTY_VIEW_TYPE
            }
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        try {
            when (getItemViewType(position)) {
                LATEST_NEWS -> {
                    val holder: NewsViewHolder = viewHolder as NewsViewHolder
                    holder.bindItem(newsResponse[position] as Article, position)
                }
                else -> {

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(newsArticle: Article, pos: Int) {

            itemView.newsTitle.text = newsArticle.title
            itemView.newsTimestamp.text =
                "${formatDate(newsArticle.publishedAt)} => [${newsArticle.publishedAt}]"
            itemView.newsPublisher.text = newsArticle.source.name
            try {
                if (context.isFinishing)
                    Glide.with(context.applicationContext).load(newsArticle.urlToImage)
                        .into(itemView.newsThumbnailIv)
                else
                    Glide.with(context).load(newsArticle.urlToImage)
                        .into(itemView.newsThumbnailIv)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

            itemView.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(newsArticle.url))
                context.startActivity(browserIntent)
            }
        }
    }

    private fun formatDate(publishedAt: String): String {
        //Format Feb 10, 2020
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        var ago = ""
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        try {
            val time: Long = sdf.parse(publishedAt).getTime()
            val now = System.currentTimeMillis()
            ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
                .toString()
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ago

        //Format => 10-02-2020
//        val inputFormatter: DateTimeFormatter =
//                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
//        val outputFormatter: DateTimeFormatter =
//                DateTimeFormatter.ofPattern("dd-MM-yyy", Locale.ENGLISH)
//        val date: LocalDate = LocalDate.parse(publishedAt, inputFormatter)
//        val formattedDate: String = outputFormatter.format(date)
//        return formattedDate
    }

    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // do nothing
    }
}