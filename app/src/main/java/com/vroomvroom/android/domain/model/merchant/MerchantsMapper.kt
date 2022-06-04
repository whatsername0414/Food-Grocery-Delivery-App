package com.vroomvroom.android.domain.model.merchant

import com.vroomvroom.android.domain.DomainMapper
import com.vroomvroom.android.utils.Utils.isOpen

class MerchantsMapper : DomainMapper<List<MerchantDto>, Merchants> {
    override fun mapToDomainModel(model: List<MerchantDto>): Merchants {
        return Merchants(
            mapToMerchantsList(model)
        )

    }
    private fun mapToMerchantsList(
        merchants: List<MerchantDto>
    ) : List<Merchant> {
        return merchants.map { merchant ->
            mapToMerchant(merchant)
        }
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
}