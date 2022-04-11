package com.vroomvroom.android.domain.model.product

import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.domain.DomainMapper

class ProductMapper : DomainMapper<MerchantQuery.Product, Product> {
    override fun mapToDomainModel(model: MerchantQuery.Product): Product {

        return Product(
            model._id,
            model.name,
            model.product_img_url,
            model.price,
            model.description,
            fromProductOptionList(model.option)
        )
    }

    private fun fromProductOptionList(options: List<MerchantQuery.Option>?) : List<Option>? {
        options?.let {
            return it.map { option ->
                mapFromProductOption(option)
            }
        }
        return null
    }

    private fun mapFromProductOption(option: MerchantQuery.Option) : Option {
        return Option (
            option.name,
            option.required,
            option.choice
        )
    }
}