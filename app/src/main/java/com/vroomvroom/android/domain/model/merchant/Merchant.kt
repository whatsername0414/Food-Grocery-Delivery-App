package com.vroomvroom.android.domain.model.merchant

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Merchant(
    val id: String,
    val name: String,
    val rates: Int?,
    val ratings: Double?,
    val location: List<String>?,
    val opening: String,
    val closing: String
) : Parcelable

data class Merchants(
    val data: MutableList<MerchantData?>
)

data class MerchantData(
    val _id: String,
    val name: String,
    val img_url: String,
    val categories: List<String?>,
    val rates: Int?,
    val ratings: Double?,
    val favorite: Boolean?,
    val opening: String,
    val isOpen: Boolean
)
