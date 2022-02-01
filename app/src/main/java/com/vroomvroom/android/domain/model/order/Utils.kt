package com.vroomvroom.android.domain.model.order

import com.vroomvroom.android.domain.db.cart.CartItemChoiceEntity
import com.vroomvroom.android.domain.db.cart.CartItemWithChoice
import com.vroomvroom.android.domain.db.user.UserLocationEntity

class OrderInputBuilder {
    fun builder(
        payment: Payment,
        deliveryFee: Double,
        totalPrice: Double,
        merchantId: String,
        userLocationEntity: UserLocationEntity,
        products: List<CartItemWithChoice>
    ): Order {
        return Order(
            paymentInput(payment),
            merchantId,
            locationInput(userLocationEntity),
            OrderDetail(
                deliveryFee,
                totalPrice,
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

    private fun locationInput(locationEntity: UserLocationEntity): DeliveryAddress {
        return DeliveryAddress(
            locationEntity.address!!,
            locationEntity.city!!,
            locationEntity.addInfo,
            listOf(locationEntity.latitude, locationEntity.longitude)
        )
    }

    private fun orderProductInputList(products: List<CartItemWithChoice>) : List<OrderProduct> {
        return products.map {
            orderProductInput(it)
        }
    }

    private fun orderProductInput(product: CartItemWithChoice) : OrderProduct {
        return OrderProduct(
            product.cartItemEntity.product_id,
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