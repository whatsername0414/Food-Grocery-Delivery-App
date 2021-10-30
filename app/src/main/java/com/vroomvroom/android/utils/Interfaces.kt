package com.vroomvroom.android.utils

import com.google.firebase.auth.FirebaseUser
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.view.state.ViewState

interface OnProductClickListener {
    fun onClick(product: MerchantQuery.Product_by_category?)
}

interface CurrentUserCallback {
    fun getCurrentUser(result: ViewState<FirebaseUser>)
}

interface IdTokenCallback {
    fun idToken(result: ViewState<String>)
}

interface TaskListener {
    fun newLoggedInUser(result: ViewState<FirebaseUser>)
}

interface PasswordResetCallback {
    fun resetPassword(result: ViewState<String>)
}