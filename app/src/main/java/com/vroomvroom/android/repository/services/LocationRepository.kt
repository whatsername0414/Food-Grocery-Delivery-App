package com.vroomvroom.android.repository.services

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.mapbox.geojson.Point

interface LocationRepository {
    fun initMapBoxDirectionClient(mapBoxAccessToken: String)
    fun getDirection(coordinates: MutableLiveData<List<LatLng>>)
    fun getCoordinates(points: List<Point>): List<LatLng>
    fun getLatLng(point: Point): LatLng
}