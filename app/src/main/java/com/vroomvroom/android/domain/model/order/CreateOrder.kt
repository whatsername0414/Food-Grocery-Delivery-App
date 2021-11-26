package com.vroomvroom.android.domain.model.order

import com.google.gson.annotations.SerializedName
import com.vroomvroom.android.CreateOrderMutation

data class CreateOrder (
	@SerializedName("payment") val payment : Payment,
	@SerializedName("order_detail") val orderDetail: OrderDetail,
)

data class OrderDetail (
	@SerializedName("delivery_fee") val deliveryFee : Int,
	@SerializedName("total_price") val totalPrice : Int,
	@SerializedName("merchant") val merchant: OrderDetailMerchant,
	@SerializedName("product") val product : List<OrderProduct>,
)

data class OrderDetailMerchant (
	@SerializedName("id") val id: String,
	@SerializedName("id") val name: String,
)

data class OrderProductOption (
	@SerializedName("name") val name : String,
	@SerializedName("additional_price") val additional_price : Int?,
	@SerializedName("option_type") val option_type : String
)

data class Payment (
	@SerializedName("method") val method : String,
	@SerializedName("reference") val reference : String?,
)

data class OrderProduct (
	@SerializedName("name") val name : String,
	@SerializedName("product_img_url") val product_img_url : String?,
	@SerializedName("price") val price : Int,
	@SerializedName("quantity") val quantity : Int,
	@SerializedName("instructions") val instructions : String?,
	@SerializedName("option") val option : List<OrderProductOption>?
)