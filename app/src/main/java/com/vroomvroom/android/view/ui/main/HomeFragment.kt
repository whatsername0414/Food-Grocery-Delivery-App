package com.vroomvroom.android.view.ui.main

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentHomeBinding
import com.vroomvroom.android.view.adapter.HomeAdapter
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.Constants
import com.vroomvroom.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.io.IOException

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment: Fragment(), OnMapReadyCallback {

    private val viewModel by activityViewModels<MainViewModel>()
    private val groupList: MutableList<String> = mutableListOf()
    private val homeAdapter by lazy { HomeAdapter(requireContext(), groupList) }
    private var mapView: MapView? = null
    private var map: GoogleMap? = null

    private var coordinates: LatLng? = null
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        mapView = binding.locationBottomSheet.userLocationMapView
        initGoogleMap(savedInstanceState)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val supportFragManager = activity?.supportFragmentManager
        supportFragManager?.commit {
            setReorderingAllowed(true)
        }
        binding.darkBg.visibility = View.GONE
        binding.homeConnectionFailedNotice.visibility = View.GONE

        viewModel.location.observe(viewLifecycleOwner, { location ->
            if (location != null) {
                initBottomSheet(location)
            }
        })

        viewModel.currentLocation.observe(viewLifecycleOwner, { location ->
            if (location != null) {
                initBottomSheet(location)
            }
        })
        groupList.clear()
        groupList.add("Main\nCategory")
        groupList.add("Merchants")

        viewModel.queryHomeData()
        observeLiveData()

        binding.homeRv.layoutManager = LinearLayoutManager(requireContext())
        binding.homeRv.adapter = homeAdapter

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.locationBottomSheet.root)
        viewModel.isLocationBottomSheetActive.observe(viewLifecycleOwner, { active ->
            binding.locationCv.setOnClickListener {
                if (!active) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    binding.darkBg.visibility = View.VISIBLE
                    binding.homeRetryButton.isClickable = false
                    viewModel.isLocationBottomSheetActive.postValue(true)
                    initMapView()
                } else {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    binding.darkBg.visibility = View.GONE
                    binding.homeRetryButton.isClickable = true
                    viewModel.isLocationBottomSheetActive.postValue(false)
                }
            }
        })

        binding.locationBottomSheet.btnAddAddress.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            findNavController().navigate(R.id.action_homeFragment_to_mapsFragment)
        }

        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
            @SuppressLint("SwitchIntDef")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.darkBg.visibility = View.GONE
                        binding.homeRetryButton.isClickable = true
                        viewModel.isLocationBottomSheetActive.postValue(false)
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.darkBg.visibility = View.VISIBLE
                        binding.homeRetryButton.isClickable = false
                        viewModel.isLocationBottomSheetActive.postValue(true)
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        binding.darkBg.visibility = View.VISIBLE
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.homeRetryButton.isClickable = true
                viewModel.isLocationBottomSheetActive.postValue(false)
            }

        })

        binding.darkBg.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            binding.darkBg.visibility = View.GONE
            binding.homeRetryButton.isClickable = true
            viewModel.isLocationBottomSheetActive.postValue(false)
        }

        homeAdapter.categoryAdapter.onCategoryClicked = { category ->
            category.let {
                if (category?.name != null) {
                    viewModel.queryMerchantByCategory(category.name)
                } else {
                    viewModel.queryMerchantByCategory("")
                }
                observeRestaurantLiveData()
            }
        }
        homeAdapter.merchantAdapter.onMerchantClicked = { merchant ->
            binding.fetchProgress.visibility = View.VISIBLE
            merchant.let {
                if (merchant.id.isNotBlank()) {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToMerchantFragment(merchant.id)
                    )
                }
            }

        }

        binding.homeRetryButton.setOnClickListener {
            viewModel.queryHomeData()
            observeLiveData()
        }
    }

    private fun initBottomSheet(location: Any) {
        if (location is String) {
            val stringCoordinates = location.split(", ")
            coordinates = LatLng(stringCoordinates[0].toDouble(), stringCoordinates[1].toDouble())
        } else if (location is Location) {
            coordinates =  LatLng(location.latitude, location.longitude)
        } else coordinates = null

        val geoCoder = Geocoder(requireContext())
        if (coordinates != null) {
            try {
                val currentLocation = geoCoder.getFromLocation(
                    coordinates!!.latitude,
                    coordinates!!.longitude,
                    1
                )
                binding.addressTv.text =
                    currentLocation.first().thoroughfare ?: "Street not provided"
                binding.cityTv.text =
                    currentLocation.first().locality ?: "City not provided"
                binding.locationBottomSheet.bsAddress.text =
                    currentLocation.first().thoroughfare ?: "Street not provided"
                binding.locationBottomSheet.bsCity.text =
                    currentLocation.first().locality ?: "City not provided"
            } catch (e: IOException) {
                Toast.makeText(requireContext(), "Unknown error occurred", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constants.MAPVIEW_BUNDLE_KEY)
        }
        mapView?.onCreate(mapViewBundle)
        mapView?.getMapAsync(this)
    }

    private fun initMapView() {
        val userLocation = coordinates ?: LatLng(13.3590302,123.7298568)
        map?.mapType = GoogleMap.MAP_TYPE_NORMAL
        map?.uiSettings?.setAllGesturesEnabled(false)
        map?.addMarker(MarkerOptions().position(userLocation).icon(bitmapDescriptorFromVector(R.drawable.ic_location)))
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15.8f))
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

    private fun observeLiveData() {
        viewModel.homeData.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.homeConnectionFailedNotice.visibility = View.GONE
                    binding.homeRv.visibility = View.GONE
                    binding.fetchProgress.visibility = View.VISIBLE
                }
                is ViewState.Success -> {
                    if (response.value?.data == null) {
                        homeAdapter.categoryAdapter.submitList(emptyList())
                        homeAdapter.merchantAdapter.submitList(emptyList())
                        binding.fetchProgress.visibility = View.GONE
                        binding.homeRv.visibility = View.GONE
                        binding.homeRv.visibility = View.GONE
                        binding.homeConnectionFailedNotice.visibility = View.VISIBLE
                    } else {
                        binding.homeRv.visibility = View.VISIBLE
                        binding.homeRv.visibility = View.VISIBLE
                        binding.homeConnectionFailedNotice.visibility = View.GONE
                    }
                    val category = response.value?.data?.getCategories
                    val merchant = response.value?.data?.getMerchants
                    homeAdapter.categoryAdapter.submitList(category)
                    homeAdapter.merchantAdapter.submitList(merchant)
                    binding.fetchProgress.visibility = View.GONE
                }
                is ViewState.Error -> {
                    homeAdapter.categoryAdapter.submitList(emptyList())
                    homeAdapter.merchantAdapter.submitList(emptyList())
                    binding.fetchProgress.visibility = View.GONE
                    binding.homeRv.visibility = View.GONE
                    binding.homeRv.visibility = View.GONE
                    binding.homeConnectionFailedNotice.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun observeRestaurantLiveData() {
        viewModel.homeData.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.homeRv.visibility = View.VISIBLE
                    binding.homeRv.visibility = View.VISIBLE
                    binding.fetchProgress.visibility = View.VISIBLE
                }
                is ViewState.Success -> {
                    if (response.value?.data == null) {
                        homeAdapter.merchantAdapter.submitList(emptyList())
                        binding.fetchProgress.visibility = View.GONE
                        binding.homeRv.visibility = View.GONE
                        binding.homeConnectionFailedNotice.visibility = View.VISIBLE
                    } else {
                        binding.homeConnectionFailedNotice.visibility = View.GONE
                    }
                    val merchant = response.value?.data?.getMerchants
                    homeAdapter.merchantAdapter.submitList(merchant)
                    binding.fetchProgress.visibility = View.GONE
                }
                is ViewState.Error -> {
                    homeAdapter.categoryAdapter.submitList(emptyList())
                    homeAdapter.merchantAdapter.submitList(emptyList())
                    binding.fetchProgress.visibility = View.GONE
                    binding.homeRv.visibility = View.GONE
                    binding.homeRv.visibility = View.GONE
                    binding.homeConnectionFailedNotice.visibility = View.VISIBLE
                }
            }
        }
    }
    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(requireContext(), vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}