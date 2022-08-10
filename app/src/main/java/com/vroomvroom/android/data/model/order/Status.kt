package com.vroomvroom.android.data.model.order

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class Status : Parcelable {
    PENDING,
    CONFIRMED,
    TO_RECEIVE,
    DELIVERED,
    CANCELLED
}