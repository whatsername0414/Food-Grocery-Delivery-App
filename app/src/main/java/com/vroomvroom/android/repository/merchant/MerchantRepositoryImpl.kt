package com.vroomvroom.android.repository.merchant

import android.util.Log
import com.vroomvroom.android.data.api.MerchantService
import com.vroomvroom.android.data.model.merchant.*
import com.vroomvroom.android.repository.BaseRepository
import com.vroomvroom.android.view.resource.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MerchantRepositoryImpl @Inject constructor(
    private val service: MerchantService,
    private val merchantMapper: MerchantMapper
) : MerchantRepository, BaseRepository()  {
    //TODO handle error body response

    override suspend fun getCategories(type: String): Resource<List<Category>>? {
        var data: Resource<List<Category>>? = null
        try {
            val result = service.getCategories(type)
            if (result.isSuccessful) {
                result.body()?.data?.let {
                    data = handleSuccess(it)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun getMerchants(category: String?, searchTerm: String?): Resource<List<Merchant>>? {
        var data: Resource<List<Merchant>>? = null
        try {
            val result = service.getMerchants(category, searchTerm)
            if (result.isSuccessful) {
                result.body()?.data?.let {
                    withContext(Dispatchers.Default) {
                        val merchants = merchantMapper.mapToDomainModelList(it)
                        data = handleSuccess(merchants)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun getMerchant(id: String): Resource<Merchant>? {
        var data: Resource<Merchant>? = null
        try {
            val result = service.getMerchant(id)
            if (result.isSuccessful) {
                result.body()?.data?.let {
                    withContext(Dispatchers.Default) {
                        data = handleSuccess(merchantMapper.mapToDomainModel(it))
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun getFavorites(): Resource<List<Merchant>>? {
        var data: Resource<List<Merchant>>? = null
        try {
            val result = service.getFavorites()
            if (result.isSuccessful) {
                result.body()?.data?.let {
                    withContext(Dispatchers.Default) {
                        data = handleSuccess(merchantMapper.mapToDomainModelList(it))
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun updateFavorite(id: String): Resource<Boolean> {
        val data: Resource<Boolean>
        try {
            val result = service.updateFavorite(id)
            data = if (result.isSuccessful && result.code() == 201) {
                handleSuccess(true)
            } else {
                handleSuccess(false)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    companion object {
        const val TAG = "MerchantRepositoryImpl"
    }
}