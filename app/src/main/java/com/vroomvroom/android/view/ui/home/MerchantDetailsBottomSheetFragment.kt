package com.vroomvroom.android.view.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vroomvroom.android.databinding.FragmentMerchantDetailsBottomSheetBinding
import com.vroomvroom.android.utils.Constants
import com.vroomvroom.android.utils.Utils.geoCoder
import com.vroomvroom.android.utils.Utils.setMap

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
        binding.merchantDetails = args.merchant
        Log.d("MerchantDetailsBottomSheetFragment", args.merchant.toString())
        args.merchant.location?.let { location ->
            merchantLocation = LatLng(
                location[0].toDouble(),
                location[1].toDouble()
            )
        }
        initGoogleMap(savedInstanceState)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val address = geoCoder(requireContext(), merchantLocation)
        address?.let {
            binding.locationDetailBottomSheet.text = "${it.thoroughfare}, ${it.locality}"
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setMap(requireContext(), merchantLocation)
    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constants.MAPVIEW_BUNDLE_KEY)
        }
        mapView?.onCreate(mapViewBundle)
        mapView?.getMapAsync(this)
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