package com.vroomvroom.android.domain.model.order

import com.apollographql.apollo.api.toInput
import com.vroomvroom.android.domain.DomainMapper
import com.vroomvroom.android.type.*

class OrderInputMapper: DomainMapper<Order, OrderInput> {
    override fun mapToDomainModel(model: Order): OrderInput {
        return OrderInput(
            mapToPaymentInput(model.payment),
            model.merchant,
            mapToLocationInput(model.deliveryAddress),
            OrderDetailInput(
                model.orderDetail.deliveryFee,
                model.orderDetail.totalPrice,
                mapToOrderProductInputList(model.orderDetail.product)
            )
        )
    }

    private fun mapToPaymentInput(payment: Payment): PaymentInput {
        return PaymentInput(
            payment.reference.toInput(),
            payment.method
        )
    }

    private fun mapToLocationInput(deliveryAddress: DeliveryAddress): LocationInput {
        return LocationInput(
            deliveryAddress.address.toInput(),
            deliveryAddress.city.toInput(),
            deliveryAddress.addInfo.toInput(),
            deliveryAddress.coordinates
        )
    }

    private fun mapToOrderProductInputList(orderProducts: List<OrderProduct>): List<OrderProductInput> {
        return orderProducts.map {
            mapToOrderProductInput(it)
        }
    }

    private fun mapToOrderProductInput(orderProduct: OrderProduct): OrderProductInput {
        return OrderProductInput(
            orderProduct.product_id,
            orderProduct.name,
            orderProduct.product_img_url.toInput(),
            orderProduct.price,
            orderProduct.quantity,
            orderProduct.instructions.toInput(),
            mapToOrderProductOptionInputList(orderProduct.option).toInput()
        )
    }

    private fun mapToOrderProductOptionInputList(
        orderProductOptions: List<OrderProductOption>?
    ) : List<OrderProductOptionInput?>? {
        return orderProductOptions?.map {
            mapToOrderProductOptionInput(it)
        }
    }

    private fun mapToOrderProductOptionInput(orderProductOption: OrderProductOption) : OrderProductOptionInput {
        return OrderProductOptionInput(
            orderProductOption.name,
            orderProductOption.additional_price.toInput(),
            orderProductOption.option_type
        )
    }
}