package com.vroomvroom.android.data.model.merchant

data class MerchantDto(
    val _id: String,
    val name: String,
    val img_url: String,
    val categories: List<String>,
    val product_sections: List<ProductSectionsDto>?,
    val rates: Int?,
    val ratings: Double?,
    var favorite: Boolean?,
    val location: List<Double>?,
    val opening: Int,
    val closing: Int,
    val isOpen: Boolean,
    val reviews: List<ReviewDto>?
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
    val option_sections: List<OptionSectionDto>?
)

data class OptionSectionDto(
    val name: String,
    val required: Boolean,
    val options: List<OptionDto>
)

data class OptionDto(
    val name: String,
    val additional_price: Double?
)

data class ReviewDto(
    val _id: String,
    val user_id: String,
    val comment: String?,
    val rate: Int,
    val created_at: String
)
