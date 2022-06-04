package com.vroomvroom.android.domain.model.merchant

data class MerchantDto(
    val _id: String,
    val name: String,
    val img_url: String,
    val categories: List<String>,
    val product_sections: List<ProductSectionsDto>?,
    val rates: Int?,
    val ratings: Double?,
    var favorite: Boolean?,
    val location: List<String>?,
    val opening: Int,
    val closing: Int,
    val reviews: List<ReviewsDto>?
)

data class ProductSectionsDto(
    val _id: String,
    val name: String,
    val products: List<ProductDto>
)

data class ProductDto(
    val _id: String,
    val name: String,
    val product_img_url: String?,
    val price: Double,
    val description: String?,
    val option: List<OptionDto>?
)

data class OptionDto(
    val name: String,
    val required: Boolean,
    val choice: List<ChoiceDto>
)

data class ChoiceDto(
    val name: String,
    val additional_price: Double?
)

data class ReviewsDto(
    val _id: String,
    val user_id: String,
    val _review: String?,
    val rate: Int,
    val created_at: String
)
