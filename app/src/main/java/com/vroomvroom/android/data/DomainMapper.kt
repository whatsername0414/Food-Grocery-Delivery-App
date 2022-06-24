package com.vroomvroom.android.data

interface DomainMapper<T, DomainModel> {
    fun mapToDomainModel(model: T): DomainModel
    fun mapToDomainModelList(model: List<T>): List<DomainModel>
    fun mapFromDomainModel(model: DomainModel): T
}