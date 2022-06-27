package com.vroomvroom.android.repository.merchant

import com.vroomvroom.android.data.model.merchant.Category
import com.vroomvroom.android.data.model.merchant.Merchant
import com.vroomvroom.android.view.resource.Resource

interface MerchantRepository {

    suspend fun getCategories(type: String): Resource<List<Category>>?
    suspend fun getMerchants(category: String?, searchTerm: String?): Resource<List<Merchant>>?
    suspend fun getMerchant(id: String): Resource<Merchant>?
    suspend fun getFavorites(): Resource<List<Merchant>>?
    suspend fun updateFavorite(id: String): Resource<Boolean>

}