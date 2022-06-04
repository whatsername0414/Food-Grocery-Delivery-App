package com.vroomvroom.android.repository.merchant

import com.vroomvroom.android.domain.model.merchant.Category
import com.vroomvroom.android.domain.model.merchant.Merchant
import com.vroomvroom.android.domain.model.merchant.Merchants
import com.vroomvroom.android.view.state.ViewState

interface MerchantRepository {

    suspend fun getCategories(type: String): ViewState<List<Category>>?
    suspend fun getMerchants(category: String?, searchTerm: String?): ViewState<Merchants>?
    suspend fun getMerchant(id: String): ViewState<Merchant>?

}