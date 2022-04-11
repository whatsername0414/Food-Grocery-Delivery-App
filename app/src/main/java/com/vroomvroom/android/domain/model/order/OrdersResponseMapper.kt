package com.vroomvroom.android.domain.model.order

import com.vroomvroom.android.OrderQuery
import com.vroomvroom.android.OrdersByStatusQuery
import com.vroomvroom.android.domain.DomainMapper

class OrdersResponseMapper : DomainMapper<OrdersByStatusQuery.Data, List<OrderResponse?>> {
    override fun mapToDomainModel(model: OrdersByStatusQuery.Data): List<OrderResponse?> {
        return model.getOrdersByStatus.map { mapToOrderResponse(it) }
    }

    private fun mapToOrderResponse( order: OrdersByStatusQuery.GetOrdersByStatus?): OrderResponse? {
        return order?.let {
            OrderResponse(
                it.id,
                mapToCustomer(it.customer),
                mapToMerchant(it.merchant),
                mapToPayment(it.payment),
                mapToDeliveryAddress(it.delivery_address),
                mapToOrderDetail(it.order_detail),
                it.status,
                it.created_at.toString(),
                false
            )
        }
    }

    private fun mapToCustomer(customer: OrdersByStatusQuery.Customer): Customer {
        return Customer(null, mapToPhone(customer.phone))
    }
    private fun mapToPhone(phone: OrdersByStatusQuery.Phone?): Phone? {
        return phone?.let { Phone(it.number, it.verified) }
    }
    private fun mapToMerchant(merchant: OrdersByStatusQuery.Merchant): Merchant {
        return Merchant(merchant._id, merchant.name)
    }
    private fun mapToPayment(payment: OrdersByStatusQuery.Payment): PaymentResponse {
        return PaymentResponse(payment.method, payment.created_at.toString())
    }
    private fun mapToDeliveryAddress(
        address: OrdersByStatusQuery.Delivery_address): DeliveryAddress {
        return DeliveryAddress(
            address.address, address.city, address.additional_information, address.coordinates)
    }
    private fun mapToOrderDetail(order: OrdersByStatusQuery.Order_detail): OrderDetail {
        return OrderDetail(order.delivery_fee, order.total_price, mapToOrderProducts(order.product))
    }
    private fun mapToOrderProducts(
        products: List<OrdersByStatusQuery.Product>): List<OrderProduct> {
        return products.map { mapToOrderProduct(it) }
    }

    private fun mapToOrderProduct(product: OrdersByStatusQuery.Product): OrderProduct {
        return OrderProduct(
            product.id,
            product.product_id,
            product.name,
            product.product_img_url,
            product.price,
            product.quantity,
            null,
            mapToProductOptions(product.option)
        )
    }

    private fun mapToProductOptions(options: List<OrdersByStatusQuery.Option>?
    ): List<OrderProductOption>? {
        return options?.map { mapToProductOption(it) }
    }

    private fun mapToProductOption(option: OrdersByStatusQuery.Option): OrderProductOption {
        return OrderProductOption(option.name, option.additional_price, option.option_type)
    }
}

class OrderResponseMapper : DomainMapper<OrderQuery.Data, OrderResponse> {
    override fun mapToDomainModel(model: OrderQuery.Data): OrderResponse {
        return model.getOrder.let {
            OrderResponse(
                it.id,
                mapToCustomer(it.customer),
                mapToMerchant(it.merchant),
                mapToPayment(it.payment),
                mapToDeliveryAddress(it.delivery_address),
                mapToOrderDetail(it.order_detail),
                it.status,
                it.created_at.toString(),
                it.reviewed
            )
        }
    }

    private fun mapToCustomer(customer: OrderQuery.Customer): Customer {
        return Customer(customer.name, mapToPhone(customer.phone))
    }
    private fun mapToPhone(phone: OrderQuery.Phone?): Phone? {
        return phone?.let { Phone(it.number, it.verified) }
    }
    private fun mapToMerchant(merchant: OrderQuery.Merchant): Merchant {
        return Merchant(merchant._id, merchant.name)
    }
    private fun mapToPayment(payment: OrderQuery.Payment): PaymentResponse {
        return PaymentResponse(payment.method, payment.created_at.toString())
    }
    private fun mapToDeliveryAddress(
        address: OrderQuery.Delivery_address): DeliveryAddress {
        return DeliveryAddress(
            address.address, address.city, address.additional_information, address.coordinates)
    }
    private fun mapToOrderDetail(order: OrderQuery.Order_detail): OrderDetail {
        return OrderDetail(order.delivery_fee, order.total_price, mapToOrderProducts(order.product))
    }
    private fun mapToOrderProducts(
        products: List<OrderQuery.Product>): List<OrderProduct> {
        return products.map { mapToOrderProduct(it) }
    }

    private fun mapToOrderProduct(product: OrderQuery.Product): OrderProduct {
        return OrderProduct(
            product.id,
            product.product_id,
            product.name,
            product.product_img_url,
            product.price,
            product.quantity,
            null,
            mapToProductOptions(product.option)
        )
    }

    private fun mapToProductOptions(options: List<OrderQuery.Option>?
    ): List<OrderProductOption>? {
        return options?.map { mapToProductOption(it) }
    }

    private fun mapToProductOption(option: OrderQuery.Option): OrderProductOption {
        return OrderProductOption(option.name, option.additional_price, option.option_type)
    }
}