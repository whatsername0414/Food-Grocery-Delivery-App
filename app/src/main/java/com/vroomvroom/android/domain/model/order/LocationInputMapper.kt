package com.vroomvroom.android.domain.model.order

import com.apollographql.apollo.api.toInput
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.domain.DomainMapper
import com.vroomvroom.android.type.LocationInput

class LocationInputMapper: DomainMapper<UserLocationEntity, LocationInput> {
    override fun mapToDomainModel(model: UserLocationEntity): LocationInput {
        return LocationInput(
            model.address.toInput(),
            model.city.toInput(),
            model.addInfo.toInput(),
            listOf(model.latitude, model.longitude)
        )
    }
}