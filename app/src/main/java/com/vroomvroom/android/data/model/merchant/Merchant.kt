package com.vroomvroom.android.data.model.merchant

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

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
    val optionTypes: @RawValue List<OptionType>?
) : Parcelable

data class OptionType(
    val name: String,
    val required: Boolean,
    val options: List<Option>
)

data class Option(
    val name: String,
    val additionalPrice: Double?
)

data class Review(
    val id: String,
    val userId: String,
    val comment: String?,
    val rate: Int,
    val createdAt: Long
)
