package com.vroomvroom.android.domain.mapper

interface DomainMapper<T, DomainModel> {
    fun mapToDomainModel(model: T): DomainModel
}