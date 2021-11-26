package com.vroomvroom.android.domain.model.order

import com.apollographql.apollo.api.toInput
import com.vroomvroom.android.domain.mapper.DomainMapper
import com.vroomvroom.android.type.*

class OrderInputMapper : DomainMapper<CreateOrder, OrderInput> {
    override fun mapToDomainModel(model: CreateOrder): OrderInput {
        return OrderInput(
            mapToPaymentInput(model.payment),
            OrderDetailInput(
                model.orderDetail.deliveryFee,
                model.orderDetail.totalPrice,
                mapToOrderDetailMerchantInput(model.orderDetail.merchant),
                mapToOrderProductInputList(model.orderDetail.product)
            )
        )
    }

    private fun mapToOrderDetailMerchantInput(
        orderDetailMerchant: OrderDetailMerchant
    ) : OrderDetailMerchantInput {
        return OrderDetailMerchantInput(
            orderDetailMerchant.id,
            orderDetailMerchant.name
        )
    }

    private fun mapToPaymentInput(payment: Payment): PaymentInput {
        return PaymentInput(
            payment.reference.toInput(),
            payment.method
        )
    }

    private fun mapToOrderProductInputList(orderProducts: List<OrderProduct>): List<OrderProductInput> {
        return orderProducts.map {
            mapToOrderProductInput(it)
        }
    }

    private fun mapToOrderProductInput(orderProduct: OrderProduct): OrderProductInput {
        return OrderProductInput(
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