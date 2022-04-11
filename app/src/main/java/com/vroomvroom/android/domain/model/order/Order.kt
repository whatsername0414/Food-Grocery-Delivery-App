package com.vroomvroom.android.domain.model.order

data class Order (
	val payment : Payment,
	val merchant: String,
	val orderDetail: OrderDetail,
)

data class OrderDetail (
	val deliveryFee : Double,
	val totalPrice : Double,
	val product : List<OrderProduct>,
)

data class Payment (
	val method : String,
	val reference : String?,
)

data class OrderProduct (
	val product_id : String,
	val name : String,
	val product_img_url : String?,
	val price : Double,
	val quantity : Int,
	val instructions : String?,
	val option : List<OrderProductOption>?
)

data class OrderProductOption (
	val name : String,
	val additional_price : Double?,
	val option_type : String
)