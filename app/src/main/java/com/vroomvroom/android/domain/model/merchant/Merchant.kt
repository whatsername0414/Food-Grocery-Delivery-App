package com.vroomvroom.android.domain.model.merchant

data class Merchants(
    val data: MutableList<Merchant?>
)

data class Merchant(
    val _id: String,
    val name: String,
    val img_url: String,
    val categories: List<String?>,
    val rates: Int?,
    val ratings: Double?,
    var favorite: Boolean?,
    val opening: Int,
    val isOpen: Boolean
)
