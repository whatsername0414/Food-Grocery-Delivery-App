package com.vroomvroom.android.repository

import com.vroomvroom.android.view.state.ViewState

abstract class BaseRepository {

    companion object {
        const val GENERAL_ERROR_CODE = 499
        private const val UNAUTHORIZED = "Unauthorized"
        private const val WRONG_CREDENTIAL = "Invalid Password"
        private const val USER_CONFLICT = "User Already Exists"
        private const val BAD_REQUEST = "Bad Request"
        private const val NOT_FOUND = "Not found"
        private const val SOMETHING_WRONG = "Something went wrong"

        fun <T : Any> handleSuccess(data: T): ViewState<T> {
            return ViewState.Success(data)
        }

        fun <T : Any> handleException(code: Int): ViewState<T> {
            val exception = getErrorMessage(code)
            return ViewState.Error(Exception(exception))
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