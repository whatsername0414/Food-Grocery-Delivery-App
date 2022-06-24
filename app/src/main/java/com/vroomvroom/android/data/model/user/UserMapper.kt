package com.vroomvroom.android.data.model.user

import com.vroomvroom.android.data.DomainMapper

class UserMapper : DomainMapper<UserDto, UserEntity> {
    override fun mapToDomainModel(model: UserDto): UserEntity {
        return UserEntity(
            model._id,
            model.name,
            model.email,
            Phone(model.phone?.number, model.phone?.verified ?: false)
        )
    }

    override fun mapToDomainModelList(model: List<UserDto>): List<UserEntity> {
        TODO("Not yet implemented")
    }

    override fun mapFromDomainModel(model: UserEntity): UserDto {
        TODO("Not yet implemented")
    }
}