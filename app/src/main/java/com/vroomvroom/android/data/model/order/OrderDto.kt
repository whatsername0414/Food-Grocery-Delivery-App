package com.vroomvroom.android.data.model.order

import com.google.gson.annotations.SerializedName

data class OrderDto(
    @SerializedName("_id") val id: String,
    val customer: Customer,
    val merchant: Merchant,
    val payment: PaymentDto,
    @SerializedName("delivery_address") val deliveryAddress: DeliveryAddress,
    @SerializedName("order_detail") val orderDetail: OrderDetailDto,
    val status: StatusDto,
    @SerializedName("created_at") val createdAt: String,
    val reviewed: Boolean
)

data class OrderDetailDto (
    @SerializedName("delivery_fee") val deliveryFee : Double,
    @SerializedName("total_price") val totalPrice : Double,
    val products : List<OrderProductDto>,
)

data class OrderProductDto (
    @SerializedName("_id") val id: String?,
    @SerializedName("product_id") val productId : String,
    val name : String,
    @SerializedName("product_img_url") val productImgUrl : String?,
    val price : Double,
    val quantity : Int,
    val instructions : String? = null,
    val options : List<OrderProductOptionDto>?
)

data class Customer(
    val name: String?,
    val phone: Phone?
)

data class Phone(
    val number: String,
    val verified: Boolean
)

data class Merchant(
    @SerializedName("_id") val id: String,
    val name: String
)

data class PaymentDto(
    val method: String,
    @SerializedName("created_at") val createdAt: String
)

data class OrderProductOptionDto(
    val name: String,
    @SerializedName("additional_price") val additionalPrice: Double?,
    @SerializedName("option_type") val optionType: String
)

data class StatusDto(
    val label: String,
    val ordinal: Int
)