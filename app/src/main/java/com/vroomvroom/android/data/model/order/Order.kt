package com.vroomvroom.android.data.model.order

import com.google.gson.annotations.SerializedName

data class Order (
	@SerializedName("_id") val id: String?,
	val payment : Payment,
	val merchantId: String,
	val deliveryAddress: DeliveryAddress,
	val orderDetail: OrderDetail,
)

data class Payment (
	val method : String,
	val reference : String?,
)

data class DeliveryAddress (
	val address: String?,
	val city: String?,
	@SerializedName("additional_information") val addInfo: String? = null,
	val coordinates: List<Double>
)

data class OrderDetail (
	@SerializedName("delivery_fee") val deliveryFee : Double,
	@SerializedName("total_price") val totalPrice : Double,
	val product : List<OrderProduct>,
)

data class OrderProduct (
	@SerializedName("_id") val id: String?,
	@SerializedName("product_id") val productId : String,
	val name : String,
	@SerializedName("product_img_url") val productImgUrl : String?,
	val price : Double,
	val quantity : Int,
	val instructions : String? = null,
	val option : List<OrderProductOption>?
)

data class OrderProductOption (
	val name : String,
	val additionalPrice : Double?,
	val optionType : String
)