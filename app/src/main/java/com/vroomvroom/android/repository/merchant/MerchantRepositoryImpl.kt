package com.vroomvroom.android.repository.merchant

import android.util.Log
import com.vroomvroom.android.data.api.MerchantService
import com.vroomvroom.android.data.model.merchant.*
import com.vroomvroom.android.repository.BaseRepository
import com.vroomvroom.android.view.state.ViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MerchantRepositoryImpl @Inject constructor(
    private val service: MerchantService,
    private val merchantMapper: MerchantMapper
) : MerchantRepository, BaseRepository()  {
    //TODO handle error body response

    override suspend fun getCategories(type: String): ViewState<List<Category>>? {
        var data: ViewState<List<Category>>? = null
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

    override suspend fun getMerchants(category: String?, searchTerm: String?): ViewState<List<Merchant>>? {
        var data: ViewState<List<Merchant>>? = null
        try {
            val result = service.getMerchants(category, searchTerm)
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

    override suspend fun getMerchant(id: String): ViewState<Merchant>? {
        var data: ViewState<Merchant>? = null
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

    override suspend fun getFavorites(): ViewState<List<Merchant>>? {
        var data: ViewState<List<Merchant>>? = null
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

    override suspend fun updateFavorite(id: String): ViewState<Boolean> {
        val data: ViewState<Boolean>
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