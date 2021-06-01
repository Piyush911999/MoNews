package com.lodestar.monews.model


import com.google.gson.annotations.SerializedName

data class Article(
        @SerializedName("author")
        var author: String = "",
        @SerializedName("content")
        var content: String = "",
        @SerializedName("description")
        var description: String = "",
        @SerializedName("publishedAt")
        var publishedAt: String = "",
        @SerializedName("source")
        var source: Source = Source(),
        @SerializedName("title")
        var title: String = "",
        @SerializedName("url")
        var url: String = "",
        @SerializedName("urlToImage")
        var urlToImage: String = "",
        @SerializedName("timestamp")
        var timestamp: Long = 0
) : Comparable<Article> {
    override fun compareTo(other: Article): Int {
        return (this.timestamp - other.timestamp).toInt()
    }

}