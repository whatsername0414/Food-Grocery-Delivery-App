package com.vroomvroom.android.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vroomvroom.android.data.model.BaseResponse
import com.vroomvroom.android.data.model.ErrorResponse
import com.vroomvroom.android.view.resource.Resource
import okhttp3.ResponseBody

abstract class BaseRepository {

    companion object {
        const val GENERAL_ERROR_CODE = 499
        private const val UNAUTHORIZED = "Unauthorized"
        private const val WRONG_CREDENTIAL = "Invalid Password"
        private const val USER_CONFLICT = "User Already Exists"
        private const val BAD_REQUEST = "Bad Request"
        private const val NOT_FOUND = "Not found"
        private const val SOMETHING_WRONG = "Something went wrong"

        fun <T : Any> handleSuccess(data: T): Resource<T> {
            return Resource.Success(data)
        }

        fun <T : Any> handleException(
            code: Int
        ): Resource<T> {
            return Resource.Error(Exception(getErrorMessage(code)))
        }

        fun <T : Any> handleException(
            code: Int,
            res: ResponseBody? = null
        ): Resource<T> {
            val gson = Gson()
            val type = object : TypeToken<BaseResponse<ErrorResponse>>() {}.type
            val errorResponse: BaseResponse<ErrorResponse>? = gson.fromJson(res?.charStream(), type)
            val exception = errorResponse?.data?.message ?: getErrorMessage(code)
            return Resource.Error(Exception(exception))
        }

        fun <T : Any> handleException(
            code: Int,
            message: String? = null
        ): Resource<T> {
            val exception = message ?: getErrorMessage(code)
            return Resource.Error(Exception(exception))
        }

        private fun getErrorMessage(httpCode: Int): String {
            return when (httpCode) {
                400 -> BAD_REQUEST
                401 -> UNAUTHORIZED
                403 -> WRONG_CREDENTIAL
                404 -> NOT_FOUND
                409 -> USER_CONFLICT
                else -> SOMETHING_WRONG
            }
        }
    }
}