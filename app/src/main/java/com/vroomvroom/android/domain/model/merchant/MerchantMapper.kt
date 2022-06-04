package com.vroomvroom.android.domain.model.merchant

import com.vroomvroom.android.domain.DomainMapper
import com.vroomvroom.android.utils.Utils.DEFAULT_SERVER_TIME_FORMAT
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
                option = mapToOptions(it.option)
            )
        }
    }

    private fun mapToOptions(options: List<OptionDto>?): List<Option>? {
        return options?.map {
            Option(
                name = it.name,
                required = it.required,
                choice = mapToChoices(it.choice)
            )
        }
    }

    private fun mapToChoices(choices: List<ChoiceDto>): List<Choice> {
        return choices.map {
            Choice(
                name = it.name,
                additionalPrice = it.additional_price
            )
        }
    }

    private fun mapToReview(reviews: List<ReviewsDto>?): List<Review>? {
        return reviews?.map {
            Review(
                id = it._id,
                userId = it.user_id,
                rate = it.rate,
                review = it._review,
                createdAt = parseStringToTime(it.created_at, DEFAULT_SERVER_TIME_FORMAT)
            )
        }
    }
}