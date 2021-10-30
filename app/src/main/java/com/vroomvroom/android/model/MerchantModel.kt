package com.vroomvroom.android.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MerchantModel(
    val id: String,
    val name: String,
    val ratingCount: Int,
    val rating: Double,
    val location: List<String>,
    val opening: String,
    val closing: String
) : Parcelable
