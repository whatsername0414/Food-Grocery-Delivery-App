package com.vroomvroom.android.repository.services

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.core.constants.Constants.PRECISION_6
import com.mapbox.geojson.Point
import com.mapbox.geojson.utils.PolylineUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


class LocationRepositoryImpl @Inject constructor() : LocationRepository {
    private lateinit var client: MapboxDirections

    override fun initMapBoxDirectionClient(mapBoxAccessToken: String) {
        val routeOptions = RouteOptions.builder()
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .coordinates("123.721405,13.355518;123.729212,13.35662")
            .build()
        client = MapboxDirections.builder()
            .routeOptions(routeOptions)
            .accessToken(mapBoxAccessToken)
            .build()
    }


    override fun getDirection(coordinates: MutableLiveData<List<LatLng>>) {
        client.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                if (response.body() == null) {
                    Log.e(TAG,"No routes found, make sure you set the right user and access token.")
                    return
                } else if (response.body()!!.routes().size < 1) {
                    Log.e(TAG,"No routes found")
                    return
                }
                val geometry = response.body()!!.routes()[0].geometry().orEmpty()
                val points = PolylineUtils.decode(geometry, PRECISION_6)
                Log.d(TAG, points.toString())
                coordinates.postValue(getCoordinates(points))
                Log.d(TAG, coordinates.toString())
            }
            override fun onFailure(call: Call<DirectionsResponse>, throwable: Throwable) {
                Log.e(TAG,"Error: " + throwable.message)
            }
        })
    }

    override fun getCoordinates(points: List<Point>): List<LatLng> {
        return points.map {
            getLatLng(it)
        }
    }

    override fun getLatLng(point: Point): LatLng {
        return LatLng(point.latitude(), point.longitude())
    }


    companion object {
        const val TAG = "LocationRepositoryImpl"
    }
}