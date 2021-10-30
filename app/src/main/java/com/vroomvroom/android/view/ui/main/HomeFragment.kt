package com.vroomvroom.android.view.ui.main

import android.annotation.SuppressLint
import android.location.Address
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentHomeBinding
import com.vroomvroom.android.view.adapter.HomeAdapter
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.utils.Utils.customGeoCoder
import com.vroomvroom.android.utils.Utils.initLocation
import com.vroomvroom.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment: Fragment() {

    private val viewModel by activityViewModels<MainViewModel>()
    private val groupList: MutableList<String> = mutableListOf()
    private val homeAdapter by lazy { HomeAdapter(requireContext(), groupList) }


    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val supportFragManager = activity?.supportFragmentManager
        supportFragManager?.commit {
            setReorderingAllowed(true)
        }

        binding.homeConnectionFailedNotice.visibility = View.GONE

        groupList.clear()
        groupList.add("Main\nCategory")
        groupList.add("Merchants")

        viewModel.queryHomeData()
        observeLiveData()
        observeRoomCartItem()

        binding.homeRv.layoutManager = LinearLayoutManager(requireContext())
        binding.homeRv.adapter = homeAdapter

        viewModel.location.observe(viewLifecycleOwner, {
            it?.let { location ->
                val coordinates = initLocation(location)
                val address = coordinates?.let { latLong ->
                    customGeoCoder(latLong, requireContext())
                }
                updateViewOnAddressReady(address)
            }
        })

        viewModel.currentLocation.observe(viewLifecycleOwner, { location ->
            val coordinates = initLocation(location)
            val address = coordinates?.let { latLong ->
                customGeoCoder(latLong, requireContext())
            }
            updateViewOnAddressReady(address)
        })

        binding.locationCv.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_locationBottomSheetFragment)
        }

        binding.cart.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_cartBottomSheetFragment)
        }

        homeAdapter.categoryAdapter.onCategoryClicked = { category ->
            category.let {
                if (category?.name != null) {
                    viewModel.queryMerchantsByCategory(category.name)
                } else {
                    viewModel.queryMerchantsByCategory("")
                }
                observeMerchantsLiveData()
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

    private fun updateViewOnAddressReady(address: Address?) {
        viewModel.address.postValue(address)
        binding.addressTv.text =
            address?.thoroughfare ?: "Street not provided"
        binding.cityTv.text =
            address?.locality ?: "City not provided"
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
                    if (response.result.getCategories == null && response.result.getMerchants == null) {
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
                    val category = response.result.getCategories
                    val merchant = response.result.getMerchants
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

    private fun observeMerchantsLiveData() {
        viewModel.homeData.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.homeRv.visibility = View.VISIBLE
                    binding.homeRv.visibility = View.VISIBLE
                    binding.fetchProgress.visibility = View.VISIBLE
                }
                is ViewState.Success -> {
                    if (response.result.getMerchants == null) {
                        homeAdapter.merchantAdapter.submitList(emptyList())
                        binding.fetchProgress.visibility = View.GONE
                        binding.homeRv.visibility = View.GONE
                        binding.homeConnectionFailedNotice.visibility = View.VISIBLE
                    } else {
                        binding.homeConnectionFailedNotice.visibility = View.GONE
                    }
                    val merchant = response.result.getMerchants
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

    @SuppressLint("SetTextI18n")
    private fun observeRoomCartItem() {
        viewModel.cartItem.observe(viewLifecycleOwner, { items ->
            if (items.isEmpty()) {
                binding.cardCartCounter.visibility = View.GONE
            } else {
                binding.cartCounter.visibility = View.VISIBLE
                binding.cartCounter.text = "${items.size}"
            }
        })
    }
}