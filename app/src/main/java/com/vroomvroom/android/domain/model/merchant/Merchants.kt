package com.vroomvroom.android.domain.model.merchant

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

data class Merchants(
    val data: List<Merchant>
)

@Parcelize
data class Merchant(
    val id: String,
    val name: String,
    val img_url: String,
    val categories: List<String>,
    val productSections: @RawValue List<ProductSections>?,
    val rates: Int?,
    val ratings: Double?,
    var favorite: Boolean?,
    val location: List<String>?,
    val opening: Int,
    val closing: Int,
    val isOpen: Boolean,
    val reviews: @RawValue List<Review>?
) : Parcelable

data class ProductSections(
    val id: String,
    val name: String,
    val products: List<Product>
)

@Parcelize
data class Product(
    val id: String,
    val name: String,
    val productImgUrl: String?,
    val price: Double,
    val description: String?,
    val option: @RawValue List<Option>?
) : Parcelable

data class Option(
    val name: String,
    val required: Boolean,
    val choice: List<Choice>
)

data class Choice(
    val name: String,
    val additionalPrice: Double?
)

data class Review(
    val id: String,
    val userId: String,
    val review: String?,
    val rate: Int,
    val createdAt: Long
)
