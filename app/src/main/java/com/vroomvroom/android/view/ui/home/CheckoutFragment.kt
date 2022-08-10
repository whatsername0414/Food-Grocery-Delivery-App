package com.vroomvroom.android.view.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.vroomvroom.android.R
import com.vroomvroom.android.data.model.order.Payment
import com.vroomvroom.android.data.model.user.LocationEntity
import com.vroomvroom.android.databinding.FragmentCheckoutBinding
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.utils.Constants
import com.vroomvroom.android.utils.Constants.CASH_ON_DELIVERY
import com.vroomvroom.android.utils.Constants.GCASH
import com.vroomvroom.android.utils.Utils.setMap
import com.vroomvroom.android.utils.Utils.setMapWithTwoPoint
import com.vroomvroom.android.utils.Utils.showCurvedPolyline
import com.vroomvroom.android.view.resource.Resource
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.common.CommonAlertDialog
import com.vroomvroom.android.view.ui.common.CompleteType
import com.vroomvroom.android.view.ui.home.adapter.CheckoutAdapter
import com.vroomvroom.android.view.ui.home.viewmodel.CheckoutViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CheckoutFragment : BaseFragment<FragmentCheckoutBinding> (
    FragmentCheckoutBinding::inflate
), OnMapReadyCallback {

    private val checkoutViewModel by viewModels<CheckoutViewModel>()
    private val checkoutAdapter by lazy { CheckoutAdapter() }

    private var map: GoogleMap? = null
    private var mapView: MapView? = null
    private var locationEntity: LocationEntity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkoutViewModel.subtotal = 0.0
        mapView = binding.userLocationMapView
        initGoogleMap(savedInstanceState)
        navController = findNavController()
        binding.appBarLayout.toolbar.setupToolbar()
        binding.checkoutRv.adapter = checkoutAdapter

        observeUser()
        observeRoomCartItemLiveData()
        observePaymentMethod()
        observeOrderLiveData()
        observeLocation()

        binding.editLocation.setOnClickListener {
            navController.navigate(
                CheckoutFragmentDirections
                    .actionCheckoutFragmentToAddressesFragment(null)
            )
        }

        binding.editPaymentMethod.setOnClickListener {
            navController.navigate(R.id.action_checkoutFragment_to_paymentMethodFragment)
        }
    }

    private fun observeUser() {
        mainViewModel.user.observe(viewLifecycleOwner) { user ->
            when {
                user == null -> {
                    binding.btnPlaceOrder.text = getString(R.string.login_or_sign_up)
                    binding.btnPlaceOrder.setOnClickListener {
                        navController.navigate(R.id.action_checkoutFragment_to_authBottomSheetFragment)
                    }
                }
                user.phone?.number.isNullOrBlank() -> {
                    binding.btnPlaceOrder.text = getString(R.string.add_mobile_number)
                    binding.btnPlaceOrder.setOnClickListener {
                        navController.navigate(R.id.action_checkoutFragment_to_phoneVerificationFragment)
                    }
                }
                else -> {
                    observeIsLocationConfirmed()
                }
            }
        }
    }

    private fun observeRoomCartItemLiveData() {
        homeViewModel.cartItem.observe(viewLifecycleOwner) { items ->
            checkoutAdapter.submitList(items)
            checkoutViewModel.subtotal = items.sumOf { it.cartItem.price }
            binding.checkoutMerchant.text = items.first().cartItem.cartMerchant.merchantName
            binding.checkoutSubTotalTv.text =
                getString(R.string.peso, "%.2f".format(checkoutViewModel.subtotal))
        }
    }

    private fun observeLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner) { userLocation ->
            locationEntity = userLocation.find { it.currentUse }
            locationEntity?.let {
                updateLocationViews(it)
                val sydney1 =
                    LatLng(it.latitude, it.longitude)
                val sydney2 = LatLng(13.3571962,123.7297614)

                val boundsBuilder = LatLngBounds.Builder()
                    .include(sydney1)
                    .include(sydney2)
                    .build()
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(boundsBuilder, 128)
                map?.moveCamera(cameraUpdate)

                map?.setMapWithTwoPoint(requireContext(), sydney1, sydney2)
                map?.showCurvedPolyline(sydney1, sydney2, context = requireContext())
            }
        }
    }

    private fun observeIsLocationConfirmed() {
        checkoutViewModel.isLocationConfirmed.observe(viewLifecycleOwner) { confirmed ->
            if (!confirmed) {
                binding.btnPlaceOrder.text = getString(R.string.confirm_address)
                binding.btnPlaceOrder.setOnClickListener {
                    locationEntity?.let {
                        checkoutViewModel.isLocationConfirmed.postValue(true)
                    }
                }
            } else {
                binding.btnPlaceOrder.text =
                    getString(R.string.place_order,
                        "%.2f".format(checkoutViewModel.subtotal + 49))
                binding.btnPlaceOrder.setOnClickListener {
                    val cartItems = homeViewModel.cartItem.value
                    val merchant = cartItems?.first()?.cartItem?.cartMerchant
                   if (cartItems != null && locationEntity != null) {
                       checkoutViewModel.createOrder(
                           merchant?.merchantId.orEmpty(),
                           Payment(
                               mainActivityViewModel.paymentMethod.value ?: CASH_ON_DELIVERY,
                               null
                           ),
                           49.00,
                           checkoutViewModel.subtotal,
                           locationEntity!!,
                           cartItems
                       )
                   }
                }
            }
        }
    }

    private fun observeOrderLiveData() {
        checkoutViewModel.order.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    loadingDialog.show(getString(R.string.creating_order))
                }
                is Resource.Success -> {
                    homeViewModel.cartItem.removeObservers(viewLifecycleOwner)
                    homeViewModel.deleteAllCartItem()
                    loadingDialog.dismiss()
                    navController.navigate(CheckoutFragmentDirections
                        .actionCheckoutFragmentToCommonCompleteFragment(
                            R.drawable.ic_shopping_complete,
                            getString(R.string.placed_order_title),
                            getString(R.string.placed_order_message),
                            getString(R.string.view_order),
                            CompleteType.CHECKOUT,
                            response.data))
                }
                is Resource.Error -> {
                    loadingDialog.dismiss()
                    handleError()
                    binding.btnPlaceOrder.text =
                        getString(R.string.place_order,
                            "%.2f".format(checkoutViewModel.subtotal + 49))
                }
            }
        }
    }

    private fun observePaymentMethod() {
        mainActivityViewModel.paymentMethod.observe(viewLifecycleOwner) { method ->
            when (method) {
                CASH_ON_DELIVERY -> {
                    binding.imgMethod.setImageResource(R.drawable.ic_money)
                    binding.paymentMethod.text = method
                }
                GCASH -> {
                    binding.imgMethod.setImageResource(R.drawable.ic_gcash)
                    binding.paymentMethod.text = method
                }
            }
        }
    }

    private fun initGoogleMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constants.MAPVIEW_BUNDLE_KEY)
        }
        mapView?.onCreate(mapViewBundle)
        mapView?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map?.uiSettings?.isZoomControlsEnabled = false
    }

    private fun updateLocationViews(locationEntity: LocationEntity) {
        val coordinates = LatLng(locationEntity.latitude, locationEntity.longitude)
        map.setMap(requireContext(), coordinates)
        binding.checkoutAddress.text =
            locationEntity.address ?: getString(R.string.street_not_provided)
        binding.checkoutCity.text =
            locationEntity.city ?: getString(R.string.city_not_provided)
    }

    private fun handleError() {
        val dialog = CommonAlertDialog(requireActivity())
        dialog.show(
            getString(R.string.network_error),
            getString(R.string.network_error_message),
            getString(R.string.cancel),
            getString(R.string.retry),
        ) { type ->
            when (type) {
                ClickType.POSITIVE -> {
                    val cartItems = homeViewModel.cartItem.value
                    val merchant = cartItems?.first()?.cartItem?.cartMerchant
                    locationEntity?.let {
                        checkoutViewModel.createOrder(
                            merchant?.merchantId.orEmpty(),
                            Payment(
                                mainActivityViewModel.paymentMethod.value ?: CASH_ON_DELIVERY,
                                null
                            ),
                            49.00,
                            checkoutViewModel.subtotal,
                            locationEntity!!,
                            cartItems.orEmpty()
                        )
                    }
                }
                ClickType.NEGATIVE -> dialog.dismiss()
            }
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