package com.vroomvroom.android.view.ui.location.viewmodel

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.lifecycle.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.repository.local.RoomRepository
import com.vroomvroom.android.repository.services.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class LocationViewModel @Inject constructor(
    private val geocoder: Geocoder,
    private val roomRepository: RoomRepository,
    private val locationRepository: LocationRepository,
    private val fusedLocationProviderClient: FusedLocationProviderClient,
) : ViewModel() {

    private val _coordinates by lazy { MutableLiveData<List<LatLng>>() }
    val coordinates: LiveData<List<LatLng>>
        get() = _coordinates
    private val _address by lazy { MutableLiveData<Address?>() }
    val address: LiveData<Address?>
        get() = _address
    private val _geoCoderError by lazy { MutableLiveData<String>() }
    val geoCoderError: LiveData<String>
        get() = _geoCoderError
    val currentLocation by lazy { MutableLiveData<Location>() }
    val userLocation = roomRepository.getUserLocation()

    var clickedAddress: UserLocationEntity? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            currentLocation.postValue(locationResult.lastLocation)
        }
    }

    fun getAddress(coordinates: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(
                    coordinates.latitude,
                    coordinates.longitude,
                    1
                )
                if (addresses.isNotEmpty()) {
                    _address.postValue(addresses.firstOrNull())
                }
            } catch (e: Exception) {
                when (e) {
                    is IOException -> _geoCoderError.postValue("Network is unavailable")
                    is IllegalAccessException -> _geoCoderError.postValue("Unknown location")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    fun initMapBoxDirectionClient(mapBoxAccessToken: String) {
        locationRepository.initMapBoxDirectionClient(mapBoxAccessToken)
    }

    fun getDirection() {
        locationRepository.getDirection(_coordinates)
    }

    fun insertLocation(userLocationEntity: UserLocationEntity) {
        updateLocations()
        viewModelScope.launch(Dispatchers.IO) {
            roomRepository.insertLocation(userLocationEntity)
        }
    }
    fun updateLocation(userLocationEntity: UserLocationEntity) {
        updateLocations()
        viewModelScope.launch(Dispatchers.IO) {
            roomRepository.updateLocation(userLocationEntity)
        }
    }
    private fun updateLocations() {
        viewModelScope.launch(Dispatchers.IO) {
            roomRepository.updateLocations()
        }
    }
    fun deleteLocation(userLocationEntity: UserLocationEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            roomRepository.deleteLocation(userLocationEntity)
        }
    }
    fun deleteAllAddress() {
        viewModelScope.launch(Dispatchers.IO) {
            roomRepository.deleteAllAddress()
        }
    }
}