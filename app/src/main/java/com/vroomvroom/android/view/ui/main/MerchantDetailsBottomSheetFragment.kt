package com.vroomvroom.android.view.ui.main

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
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
import com.vroomvroom.android.databinding.FragmentMerchantDetailsBottomSheetBinding
import com.vroomvroom.android.utils.Constants
import com.vroomvroom.android.utils.Utils.customGeoCoder

class MerchantDetailsBottomSheetFragment : BottomSheetDialogFragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMerchantDetailsBottomSheetBinding
    private val args: MerchantDetailsBottomSheetFragmentArgs by navArgs()

    private var map: GoogleMap? = null
    private var mapView: MapView? = null
    private lateinit var merchantLocation: LatLng

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMerchantDetailsBottomSheetBinding.inflate(inflater)
        mapView = binding.userLocationMapView
        merchantLocation = LatLng(
            args.merchantDetails.location[0].toDouble(),
            args.merchantDetails.location[1].toDouble()
        )
        initGoogleMap(savedInstanceState)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.merchantDetails = args.merchantDetails
        val address = customGeoCoder(merchantLocation, requireContext())
        address?.let {
            binding.locationDetailBottomSheet.text = "${it.thoroughfare}, ${it.locality}"
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL
        map?.uiSettings?.setAllGesturesEnabled(false)
        map?.addMarker(MarkerOptions().position(merchantLocation).icon(bitmapDescriptorFromVector(R.drawable.ic_location)))
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(merchantLocation, 15.8f))
    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constants.MAPVIEW_BUNDLE_KEY)
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