package com.vroomvroom.android.utils

import android.content.Intent
import com.vroomvroom.android.OrdersQuery
import com.vroomvroom.android.domain.model.merchant.Product
import com.vroomvroom.android.view.state.ViewState


interface OnProductClickListener {
    fun onClick(product: Product?)
}

interface OnOrderProductClickListener {
    fun onClick(product: OrdersQuery.Product?)
}

interface SmsBroadcastReceiverListener {
    fun onIntent(intent: ViewState<Intent>)
}