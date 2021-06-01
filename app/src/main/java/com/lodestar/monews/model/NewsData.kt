package com.lodestar.monews.model


import com.google.gson.annotations.SerializedName

data class NewsData(
    @SerializedName("articles")
    var articles: ArrayList<Article> = ArrayList(),
    @SerializedName("status")
    var status: String = ""
)