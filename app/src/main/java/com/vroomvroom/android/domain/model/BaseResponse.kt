package com.vroomvroom.android.domain.model

data class BaseResponse<T>(
    val data: T
)

data class BaseResponseList<T>(
    val data: List<T>
)