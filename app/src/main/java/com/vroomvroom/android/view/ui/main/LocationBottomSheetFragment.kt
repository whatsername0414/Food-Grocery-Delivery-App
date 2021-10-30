package com.vroomvroom.android.view.ui.main

import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Address
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentLocationBottomSheetBinding
import com.vroomvroom.android.utils.Constants.MAPVIEW_BUNDLE_KEY
import com.vroomvroom.android.utils.Utils
import com.vroomvroom.android.viewmodel.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class LocationBottomSheetFragment : BottomSheetDialogFragment(), OnMapReadyCallback {

    private val viewModel by activityViewModels<MainViewModel>()

    private lateinit var binding: FragmentLocationBottomSheetBinding
    private var map: GoogleMap? = null
    private var mapView: MapView? = null
    private var coordinates: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationBottomSheetBinding.inflate(inflater)
        mapView = binding.userLocationMapView
        initGoogleMap(savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.location.observe(viewLifecycleOwner, {
            it?.let { location ->
                coordinates = Utils.initLocation(location)
                val address = coordinates?.let { latLong ->
                    Utils.customGeoCoder(latLong, requireContext())
                }
                updateViewOnAddressReady(address)
            }
        })

        viewModel.currentLocation.observe(viewLifecycleOwner, { location ->
            coordinates = Utils.initLocation(location)
            val address = coordinates?.let { latLong ->
                Utils.customGeoCoder(latLong, requireContext())
            }
            updateViewOnAddressReady(address)
        })

        binding.btnAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_locationBottomSheetFragment_to_mapsFragment)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val userLocation = coordinates ?: LatLng(13.3590302,123.7298568)
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL
        map?.uiSettings?.setAllGesturesEnabled(false)
        map?.addMarker(MarkerOptions().position(userLocation).icon(bitmapDescriptorFromVector(R.drawable.ic_location)))
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15.8f))
    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mapView?.onCreate(mapViewBundle)
        mapView?.getMapAsync(this)
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(requireContext(), vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    private fun updateViewOnAddressReady(address: Address?) {
        viewModel.address.postValue(address)
        binding.bsAddress.text =
            address?.thoroughfare ?: "Street not provided"
        binding.bsCity.text =
            address?.locality ?: "City not provided"
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