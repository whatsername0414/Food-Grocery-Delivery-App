package com.vroomvroom.android.data.model.user

data class UserDto(
    val _id: String,
    val name: String? = null,
    val email: String? = null,
    val phone: PhoneDto? = null
)

data class PhoneDto(
    val number: String? = null,
    val verified: Boolean = false
)
