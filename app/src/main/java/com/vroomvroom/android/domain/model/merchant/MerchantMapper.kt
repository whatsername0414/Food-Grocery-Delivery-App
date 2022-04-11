package com.vroomvroom.android.domain.model.merchant

import com.vroomvroom.android.FavoriteMerchantQuery
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.MerchantsQuery
import com.vroomvroom.android.domain.mapper.DomainMapper

class MerchantMapper : DomainMapper<MerchantQuery.GetMerchant, Merchant> {
    override fun mapToDomainModel(model: MerchantQuery.GetMerchant): Merchant {
        return Merchant(
            model._id,
            model.name,
            model.rates,
            model.ratings,
            model.location,
            model.opening,
            model.closing
        )
    }
}

class MerchantsMapper : DomainMapper<MerchantsQuery.Data, Merchants> {
    override fun mapToDomainModel(model: MerchantsQuery.Data): Merchants {
        return Merchants(
            mapToMerchantsList(model.getMerchantsByCategory)
        )

    }
    private fun mapToMerchantsList(
        merchants: List<MerchantsQuery.GetMerchantsByCategory>
    ) : MutableList<MerchantData?> {
        return merchants.map { merchant ->
            mapToMerchant(merchant)
        }.toMutableList()
    }
    private fun mapToMerchant(merchant: MerchantsQuery.GetMerchantsByCategory) : MerchantData {
        return MerchantData(
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

class FavoriteMerchantsMapper : DomainMapper<FavoriteMerchantQuery.Data, Merchants> {
    override fun mapToDomainModel(model: FavoriteMerchantQuery.Data): Merchants {
        return Merchants(
            mapToFavoriteMerchantsList(model.getFavoriteMerchants)
        )

    }
    private fun mapToFavoriteMerchantsList(
        merchants: List<FavoriteMerchantQuery.GetFavoriteMerchant?>
    ) : MutableList<MerchantData?> {
        if (merchants.isEmpty()) {
            return mutableListOf()
        }
        return merchants.map { merchant ->
            mapToFavoriteMerchant(merchant!!)
        }.toMutableList()
    }
    private fun mapToFavoriteMerchant(merchant: FavoriteMerchantQuery.GetFavoriteMerchant) : MerchantData {
        merchant.let {
            return MerchantData(
                _id = it._id,
                name = it.name,
                img_url = it.img_url,
                categories = it.categories,
                rates = it.rates,
                ratings = it.ratings,
                favorite = true,
                opening = it.opening,
                isOpen = it.isOpen
            )
        }
    }
}