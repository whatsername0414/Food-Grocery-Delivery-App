package com.vroomvroom.android.data.model.merchant

import com.vroomvroom.android.data.DomainMapper
import com.vroomvroom.android.utils.Constants.DEFAULT_SERVER_TIME_FORMAT
import com.vroomvroom.android.utils.Utils.isOpen
import com.vroomvroom.android.utils.Utils.parseStringToTime

class MerchantMapper : DomainMapper<MerchantDto, Merchant> {
    override fun mapToDomainModel(model: MerchantDto): Merchant {
        return Merchant(
            id = model._id,
            name = model.name,
            img_url = model.img_url,
            categories = model.categories,
            productSections = mapToProductSections(model.product_sections),
            rates = model.rates,
            ratings = model.ratings,
            favorite = model.favorite,
            opening = model.opening,
            closing = model.closing,
            isOpen = isOpen(model.opening, model.closing),
            location = model.location,
            reviews = mapToReview(model.reviews)
        )
    }

    private fun mapToProductSections(
        productSectionsDto: List<ProductSectionsDto>?
    ): List<ProductSections>? {
        return productSectionsDto?.map {
            ProductSections(
                id = it._id,
                name = it.name,
                products = mapToProducts(it.products)
            )
        }
    }

    private fun mapToProducts(products: List<ProductDto>): List<Product> {
        return products.map {
            Product(
                id = it._id,
                name = it.name,
                productImgUrl = it.product_img_url,
                price = it.price,
                description = it.description,
                optionTypes = mapToOptions(it.option)
            )
        }
    }

    private fun mapToOptions(options: List<OptionDto>?): List<OptionType>? {
        return options?.map {
            OptionType(
                name = it.name,
                required = it.required,
                options = mapToChoices(it.choice)
            )
        }
    }

    private fun mapToChoices(choices: List<ChoiceDto>): List<Option> {
        return choices.map {
            Option(
                name = it.name,
                additionalPrice = it.additional_price
            )
        }
    }

    private fun mapToReview(reviews: List<ReviewDto>?): List<Review>? {
        return reviews?.map {
            Review(
                id = it._id.orEmpty(),
                userId = it.user_id.orEmpty(),
                rate = it.rate,
                comment = it.comment,
                createdAt = parseStringToTime(it.created_at, DEFAULT_SERVER_TIME_FORMAT)
            )
        }
    }

    override fun mapToDomainModelList(model: List<MerchantDto>): List<Merchant> {
        return model.map { mapToMerchant(it) }
    }

    private fun mapToMerchant(merchant: MerchantDto) : Merchant {
        return Merchant(
            id = merchant._id,
            name = merchant.name,
            img_url = merchant.img_url,
            categories = merchant.categories,
            productSections = null,
            rates = merchant.rates,
            ratings = merchant.ratings,
            favorite = merchant.favorite,
            opening = merchant.opening,
            closing = merchant.closing,
            isOpen = isOpen(merchant.opening, merchant.closing),
            location = null,
            reviews = null
        )
    }

    override fun mapFromDomainModel(model: Merchant): MerchantDto {
        TODO("Not yet implemented")
    }
}