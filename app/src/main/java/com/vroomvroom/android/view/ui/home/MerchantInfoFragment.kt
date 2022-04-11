package com.vroomvroom.android.view.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentMerchantInfoBinding
import com.vroomvroom.android.utils.Constants
import com.vroomvroom.android.utils.Utils.geoCoder
import com.vroomvroom.android.utils.Utils.setMap
import com.vroomvroom.android.view.ui.activityviewmodel.ActivityViewModel
import com.vroomvroom.android.view.ui.home.adapter.ReviewAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MerchantInfoFragment : Fragment(), OnMapReadyCallback {

    private val activityViewModel by activityViewModels<ActivityViewModel>()
    private val adapter by lazy { ReviewAdapter() }
    private var map: GoogleMap? = null
    private var mapView: MapView? = null

    private lateinit var binding: FragmentMerchantInfoBinding
    private lateinit var merchantLocation: LatLng

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMerchantInfoBinding.inflate(inflater)
        mapView = binding.userLocationMapView
        initGoogleMap(savedInstanceState)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_close_maroon)

        binding.reviewRv.adapter = adapter
        val merchant = activityViewModel.merchant
        binding.merchant = merchant
        adapter.submitList(merchant.reviews)
        merchant.location?.let { location ->
            merchantLocation = LatLng(
                location[0].toDouble(),
                location[1].toDouble()
            )
        }

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