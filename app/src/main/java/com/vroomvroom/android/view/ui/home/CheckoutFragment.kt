package com.vroomvroom.android.view.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentCheckoutBinding
import com.vroomvroom.android.domain.db.user.UserLocationEntity
import com.vroomvroom.android.domain.model.order.Order
import com.vroomvroom.android.domain.model.order.OrderInputBuilder
import com.vroomvroom.android.domain.model.order.OrderInputMapper
import com.vroomvroom.android.domain.model.order.Payment
import com.vroomvroom.android.utils.ClickType
import com.vroomvroom.android.utils.Constants
import com.vroomvroom.android.utils.Utils.setMap
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.home.adapter.CheckoutAdapter
import com.vroomvroom.android.view.ui.home.viewmodel.CheckoutViewModel
import com.vroomvroom.android.view.ui.widget.CommonAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class CheckoutFragment : BaseFragment<FragmentCheckoutBinding> (
    FragmentCheckoutBinding::inflate
), OnMapReadyCallback {

    @Inject lateinit var orderInputBuilder: OrderInputBuilder
    @Inject lateinit var orderInputMapper: OrderInputMapper
    private val checkoutViewModel by viewModels<CheckoutViewModel>()
    private val checkoutAdapter by lazy { CheckoutAdapter() }

    private var map: GoogleMap? = null
    private var mapView: MapView? = null
    private var locationEntity: UserLocationEntity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.userLocationMapView
        initGoogleMap(savedInstanceState)
        navController = findNavController()
        binding.appBarLayout.toolbar.setupToolbar()
        binding.checkoutRv.adapter = checkoutAdapter

        //private functions
        observeUserRecord()
        observeRoomCartItemLiveData()
        observePaymentMethod()
        observeOrderLiveData()

        binding.btnEditAddress.setOnClickListener {
            navController.navigate(
                CheckoutFragmentDirections
                    .actionCheckoutFragmentToAddressesFragment(null)
            )
        }

        binding.editPaymentMethod.setOnClickListener {
            navController.navigate(R.id.action_checkoutFragment_to_paymentMethodFragment)
        }
    }

    private fun observeUserRecord() {
        authViewModel.userRecord.observe(viewLifecycleOwner) { user ->
            when {
                user == null -> {
                    binding.btnPlaceOrder.text = getString(R.string.login_or_sign_up)
                    binding.btnPlaceOrder.setOnClickListener {
                        navController.navigate(R.id.action_checkoutFragment_to_authBottomSheetFragment)
                    }
                }
                user.phone.number.isNullOrBlank() -> {
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
            if (!checkoutViewModel.isComputed) {
                items.forEach { item ->
                    checkoutViewModel.subtotal += item.cartItemEntity.price
                }.also { checkoutViewModel.isComputed = true }
            }
            binding.checkoutMerchant.text = items.first().cartItemEntity.cartMerchant.merchantName
            binding.checkoutSubTotalTv.text =
                getString(R.string.peso, "%.2f".format(checkoutViewModel.subtotal))
        }
    }

    private fun observeLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner) { userLocation ->
            locationEntity = userLocation.find { it.currentUse }
            locationEntity?.let {
                updateLocationViews(it)
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
                    checkoutViewModel.mutationCreateOrder(
                        orderInputMapper.mapToDomainModel(orderInputBuilder())
                    )
                }
            }
        }
    }

    private fun observeOrderLiveData() {
        checkoutViewModel.order.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ViewState.Loading -> {
                    loadingDialog.show(getString(R.string.creating_order))
                }
                is ViewState.Success -> {
                    homeViewModel.cartItem.removeObservers(viewLifecycleOwner)
                    homeViewModel.deleteAllCartItem()
                    loadingDialog.dismiss()
                    showShortToast(R.string.placed_order_message)
                    navController.navigate(R.id.action_checkoutFragment_to_homeFragment)
                }
                is ViewState.Error -> {
                    loadingDialog.dismiss()
                    initCommonDialog()
                    binding.btnPlaceOrder.text =
                        getString(R.string.place_order,
                            "%.2f".format(checkoutViewModel.subtotal + 49))
                }
            }
        }
    }

    private fun orderInputBuilder(): Order {
        val cartItems = homeViewModel.cartItem.value!!
        val merchant = cartItems.first().cartItemEntity.cartMerchant
        return orderInputBuilder.builder(
            Payment(
                mainActivityViewModel.paymentMethod.value ?: "Cash On Delivery",
                null
            ),
            49.00,
            checkoutViewModel.subtotal,
            merchant.merchantId,
            locationEntity!!,
            cartItems
        )
    }

    private fun observePaymentMethod() {
        mainActivityViewModel.paymentMethod.observe(viewLifecycleOwner) { method ->
            when (method) {
                "Cash On Delivery" -> {
                    binding.imgMethod.setImageResource(R.drawable.ic_money)
                    binding.paymentMethod.text = method
                }
                "GCash" -> {
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
        observeLocation()
    }

    private fun updateLocationViews(locationEntity: UserLocationEntity) {
        val coordinates = LatLng(locationEntity.latitude, locationEntity.longitude)
        map.setMap(requireContext(), coordinates)
        binding.checkoutAddress.text =
            locationEntity.address ?: getString(R.string.street_not_provided)
        binding.checkoutCity.text =
            locationEntity.city ?: getString(R.string.city_not_provided)
    }

    private fun initCommonDialog() {
        val dialog = CommonAlertDialog(requireActivity())
        dialog.show(
            getString(R.string.network_error),
            getString(R.string.network_error_message),
            getString(R.string.cancel),
            getString(R.string.retry),
        ) { type ->
            when (type) {
                ClickType.POSITIVE -> {
                    checkoutViewModel.mutationCreateOrder(
                        orderInputMapper.mapToDomainModel(orderInputBuilder())
                    )
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