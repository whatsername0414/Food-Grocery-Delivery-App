package com.vroomvroom.android.domain.model.merchant

import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.domain.mapper.DomainMapper

class MerchantMapper : DomainMapper<MerchantQuery.GetMerchant, Merchant> {
    override fun mapToDomainModel(model: MerchantQuery.GetMerchant): Merchant {
        return Merchant(
            model.id,
            model.name,
            model.ratingCount,
            model.rating,
            model.location,
            model.opening,
            model.closing
        )
    }
}