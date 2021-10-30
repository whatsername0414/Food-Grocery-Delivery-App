package com.vroomvroom.android.repository.services

import com.vroomvroom.android.view.state.ViewState

abstract class FirebaseAuthBaseRepository {
    companion object {
        private const val AUTHENTICATION_FAILED = "Authentication failed"
        private const val WRONG_CREDENTIAL = "Invalid password"
        private const val USER_NOT_FOUND = "User not found"
        private const val USER_CONFLICT = "User already exists"
        private const val SOMETHING_WRONG = "Something went wrong"

        fun <T : Any> handleSuccess(data: T): ViewState<T> {
            return ViewState.Success(data)
        }

        fun <T : Any> handleException(code: Int): ViewState<T> {
            val exception = getErrorMessage(code)
            return ViewState.Error(Exception(exception))
        }

        private fun getErrorMessage(code: Int): String {
            return when (code) {
                401 -> AUTHENTICATION_FAILED
                403 -> WRONG_CREDENTIAL
                404 -> USER_NOT_FOUND
                409 -> USER_CONFLICT
                else -> SOMETHING_WRONG
            }
        }
    }
}