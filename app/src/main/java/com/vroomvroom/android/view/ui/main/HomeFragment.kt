package com.vroomvroom.android.view.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentHomeBinding
import com.vroomvroom.android.domain.db.UserLocationEntity
import com.vroomvroom.android.view.adapter.CategoryAdapter
import com.vroomvroom.android.view.adapter.MerchantAdapter
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.viewmodel.LocationViewModel
import com.vroomvroom.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment: Fragment() {

    private val mainViewModel by activityViewModels<MainViewModel>()
    private val locationViewModel by viewModels<LocationViewModel>()
    private val categoryAdapter by lazy { CategoryAdapter() }
    private val merchantAdapter by lazy { MerchantAdapter() }

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.homeConnectionFailedNotice.visibility = View.GONE
        binding.fetchProgress.visibility = View.GONE

        mainViewModel.queryCategory()
        mainViewModel.queryMerchants()

        observeUserLocation()
        observeCategory()
        observeMerchants()
        observeMerchantsByCategory()
        observeRoomCartItem()

        binding.categoryRv.adapter = categoryAdapter
        binding.merchantRv.adapter = merchantAdapter

        binding.locationCv.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_locationBottomSheetFragment)
        }

        binding.cart.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_cartBottomSheetFragment)
        }

        categoryAdapter.onCategoryClicked = { category ->
            if (category?.name != null) {
                mainViewModel.queryMerchantsByCategory(category.name)
            } else {
                mainViewModel.queryMerchantsByCategory("")
            }
        }

        merchantAdapter.onMerchantClicked = { merchant ->
            if (merchant.id.isNotBlank()) {
                findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToMerchantFragment(merchant.id)
                )
            }
        }

        binding.homeRetryButton.setOnClickListener {
            mainViewModel.queryCategory()
            mainViewModel.queryMerchants()
        }
    }

    private fun updateLocationTextView(locationEntity: UserLocationEntity) {
        binding.addressTv.text =
            locationEntity.address ?: getString(R.string.street_not_provided)
        binding.cityTv.text =
            locationEntity.city ?: getString(R.string.city_not_provided)
    }

    private fun observeUserLocation() {
        locationViewModel.userLocation.observe(viewLifecycleOwner, { userLocation ->
            if (userLocation.isNullOrEmpty()) {
                findNavController().navigate(R.id.action_homeFragment_to_locationFragment)
            } else {
                val location = userLocation.first()
                updateLocationTextView(location)
            }
        })
    }

    private fun observeCategory() {
        mainViewModel.category.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.homeConnectionFailedNotice.visibility = View.GONE
                    binding.title.visibility = View.GONE
                    binding.categoryRv.visibility = View.GONE
                    binding.categoryShimmerLayout.visibility = View.VISIBLE
                    binding.categoryShimmerLayout.startShimmer()
                }
                is ViewState.Success -> {
                    val category = response.result.getCategories
                    categoryAdapter.submitList(category)
                    binding.categoryShimmerLayout.stopShimmer()
                    binding.categoryShimmerLayout.visibility = View.GONE
                    binding.title.visibility = View.VISIBLE
                    binding.categoryRv.visibility = View.VISIBLE
                }
                is ViewState.Error -> {
                    categoryAdapter.submitList(emptyList())
                    binding.categoryShimmerLayout.stopShimmer()
                    binding.categoryShimmerLayout.visibility = View.GONE
                    binding.categoryRv.visibility = View.GONE
                    binding.title.visibility = View.GONE
                    binding.homeConnectionFailedNotice.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun observeMerchants() {
        mainViewModel.merchants.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.homeConnectionFailedNotice.visibility = View.GONE
                    binding.merchantRv.visibility = View.GONE
                    binding.merchantsShimmerLayout.visibility = View.VISIBLE
                    binding.merchantsShimmerLayout.startShimmer()
                }
                is ViewState.Success -> {
                    val merchant = response.result.getMerchantsByCategory
                    merchantAdapter.submitList(merchant)
                    binding.merchantsShimmerLayout.visibility = View.GONE
                    binding.merchantsShimmerLayout.stopShimmer()
                    binding.merchantRv.visibility = View.VISIBLE
                }
                is ViewState.Error -> {
                    merchantAdapter.submitList(emptyList())
                    binding.merchantRv.visibility = View.GONE
                    binding.merchantsShimmerLayout.visibility = View.GONE
                    binding.merchantsShimmerLayout.stopShimmer()
                    binding.homeConnectionFailedNotice.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun observeMerchantsByCategory() {
        mainViewModel.merchantByCategory.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.fetchProgress.visibility = View.VISIBLE
                }
                is ViewState.Success -> {
                    val merchant = response.result.getMerchantsByCategory
                    merchantAdapter.submitList(merchant)
                    binding.fetchProgress.visibility = View.GONE
                }
                is ViewState.Error -> {
                    merchantAdapter.submitList(emptyList())
                    binding.fetchProgress.visibility = View.GONE
                    binding.merchantRv.visibility = View.GONE
                    binding.homeConnectionFailedNotice.visibility = View.VISIBLE
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeRoomCartItem() {
        mainViewModel.cartItem.observe(viewLifecycleOwner, { items ->
            if (items.isEmpty()) {
                binding.cardCartCounter.visibility = View.GONE
            } else {
                binding.cartCounter.visibility = View.VISIBLE
                binding.cartCounter.text = "${items.size}"
            }
        })
    }
}