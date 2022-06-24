package com.vroomvroom.android.data.model.merchant

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("_id") val id: String,
    val name: String,
    @SerializedName("img_url") val imageUrl: String,
    val type: String
)
