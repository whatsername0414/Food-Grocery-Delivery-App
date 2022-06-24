package com.vroomvroom.android.data.model.cart

import com.vroomvroom.android.data.model.merchant.Option

object CartItemMapper {

    fun mapToCartItemEntity(
        productId: String,
        name: String,
        productImgUrl: String?,
        price: Double,
        quantity: Int,
        specialInstructions: String?,
        merchantId: String,
        merchantName: String
    ): CartItemEntity {
        return CartItemEntity(
            productId,
            mapToMerchantEntity(merchantId, merchantName),
            name,
            productImgUrl,
            price,
            quantity,
            specialInstructions
        )
    }

    private fun mapToMerchantEntity(id: String, name: String): CartMerchantEntity {
        return CartMerchantEntity(id, name)
    }

    fun mapFromDomainModelList(model: Map<String, Option>): List<CartItemOptionEntity> {
        return model.map { (key, value) ->
            CartItemOptionEntity(null, value.name, value.additionalPrice, key)
        }
    }
}