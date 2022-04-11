package com.vroomvroom.android.domain.model.order

import com.vroomvroom.android.OrderQuery

data class OrderResponse(
    val id: String,
    val customer: Customer,
    val merchant: Merchant,
    val payment: PaymentResponse,
    val deliveryAddress: DeliveryAddress,
    val orderDetail: OrderDetail,
    val status: String,
    val created_at: String,
    val reviewed: Boolean
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
    val _id: String,
    val name: String
)

data class PaymentResponse(
    val method: String,
    val created_at: String
)

data class Option(
    val name: String,
    val additional_price: Double?,
    val option_type: String
)