package com.vroomvroom.android.domain.model.merchant

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Merchant(
    val id: String,
    val name: String,
    val ratingCount: Int,
    val rating: Double,
    val location: List<String>,
    val opening: String,
    val closing: String
) : Parcelable
