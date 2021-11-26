package com.vroomvroom.android.domain.model.order

import com.vroomvroom.android.domain.db.CartItemChoiceEntity
import com.vroomvroom.android.domain.db.CartItemWithChoice

class OrderInputBuilder {
    fun builder(
        payment: Payment,
        deliveryFee: Int,
        totalPrice: Int,
        merchantId: String,
        merchantName: String,
        products: List<CartItemWithChoice>
    ): CreateOrder {
        return CreateOrder(
            paymentInput(payment),
            OrderDetail(
                deliveryFee,
                totalPrice,
                OrderDetailMerchant(
                    merchantId,
                    merchantName
                ),
                orderProductInputList(products)
            )
        )
    }

    private fun paymentInput(payment: Payment) : Payment {
        return Payment(
            reference = payment.reference,
            method = payment.method
        )
    }

    private fun orderProductInputList(products: List<CartItemWithChoice>) : List<OrderProduct> {
        return products.map {
            orderProductInput(it)
        }
    }

    private fun orderProductInput(product: CartItemWithChoice) : OrderProduct {
        return OrderProduct(
            product.cartItemEntity.name,
            product.cartItemEntity.product_img_url,
            product.cartItemEntity.price,
            product.cartItemEntity.quantity,
            product.cartItemEntity.special_instructions,
            orderProductOptionList(product.choiceEntities)
        )
    }

    private fun orderProductOptionList(options: List<CartItemChoiceEntity>?) : List<OrderProductOption>? {
        return options?.map {
            orderProductOption(it)
        }
    }

    private fun orderProductOption(option: CartItemChoiceEntity) : OrderProductOption {
        return OrderProductOption(
            option.name,
            option.additional_price,
            option.optionType
        )
    }
}