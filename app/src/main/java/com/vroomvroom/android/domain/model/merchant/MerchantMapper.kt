package com.vroomvroom.android.domain.model.merchant

import com.vroomvroom.android.MerchantsQuery
import com.vroomvroom.android.domain.DomainMapper

class MerchantsMapper : DomainMapper<MerchantsQuery.Data, Merchants> {
    override fun mapToDomainModel(model: MerchantsQuery.Data): Merchants {
        return Merchants(
            mapToMerchantsList(model.getMerchants)
        )

    }
    private fun mapToMerchantsList(
        merchants: List<MerchantsQuery.GetMerchant?>
    ) : MutableList<Merchant?> {
        return merchants.map { merchant ->
            merchant?.let {
                mapToMerchant(it)
            }
        }.toMutableList()
    }
    private fun mapToMerchant(merchant: MerchantsQuery.GetMerchant) : Merchant {
        return Merchant(
            _id = merchant._id,
            name = merchant.name,
            img_url = merchant.img_url,
            categories = merchant.categories,
            rates = merchant.rates,
            ratings = merchant.ratings,
            favorite = merchant.favorite,
            opening = merchant.opening,
            isOpen = merchant.isOpen
        )
    }
}