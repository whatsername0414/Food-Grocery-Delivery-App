package com.vroomvroom.android.repository.merchant

import com.vroomvroom.android.data.model.merchant.Category
import com.vroomvroom.android.data.model.merchant.Merchant
import com.vroomvroom.android.view.state.ViewState

interface MerchantRepository {

    suspend fun getCategories(type: String): ViewState<List<Category>>?
    suspend fun getMerchants(category: String?, searchTerm: String?): ViewState<List<Merchant>>?
    suspend fun getMerchant(id: String): ViewState<Merchant>?
    suspend fun getFavorites(): ViewState<List<Merchant>>?
    suspend fun updateFavorite(id: String): ViewState<Boolean>

}