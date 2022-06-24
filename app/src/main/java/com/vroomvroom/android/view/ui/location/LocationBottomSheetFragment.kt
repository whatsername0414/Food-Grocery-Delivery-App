package com.vroomvroom.android.view.ui.location

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentLocationBottomSheetBinding
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.utils.Constants.MAPVIEW_BUNDLE_KEY
import com.vroomvroom.android.utils.Utils.setMap
import com.vroomvroom.android.view.ui.base.BaseBottomSheetFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LocationBottomSheetFragment : BaseBottomSheetFragment<FragmentLocationBottomSheetBinding>(
    FragmentLocationBottomSheetBinding::inflate
), OnMapReadyCallback {

    private var map: GoogleMap? = null
    private var mapView: MapView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.userLocationMapView
        initGoogleMap(savedInstanceState)

        binding.btnAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_locationBottomSheetFragment_to_mapsFragment)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        observeUserLocation()
    }

    private fun observeUserLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner) { userLocation ->
            val location = userLocation.find { it.currentUse }
            location?.let { updateLocationViews(it) }
        }
    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mapView?.onCreate(mapViewBundle)
        mapView?.getMapAsync(this)
    }

    private fun updateLocationViews(locationEntity: LocationEntity) {
        val coordinates = LatLng(locationEntity.latitude, locationEntity.longitude)
        map.setMap(requireContext(), coordinates)
        binding.bsAddress.text =
            locationEntity.address ?: getString(R.string.street_not_provided)
        binding.bsCity.text =
            locationEntity.city ?: getString(R.string.city_not_provided)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }
    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }
    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }
    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }
    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }
}