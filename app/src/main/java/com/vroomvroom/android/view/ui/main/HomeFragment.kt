package com.vroomvroom.android.view.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.vroomvroom.android.databinding.FragmentHomeBinding
import com.vroomvroom.android.view.adapter.HomeAdapter
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment: Fragment() {

    private val viewModel by viewModels<DataViewModel>()
    private val groupList: MutableList<String> = mutableListOf()
    private val homeAdapter by lazy { HomeAdapter(requireContext(), groupList) }

    private lateinit var binding: FragmentHomeBinding

    private var isBottomSheetActive = false

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

        binding.darkBg.visibility = View.GONE
        binding.homeConnectionFailedNotice.visibility = View.GONE
        viewModel.location.observe(requireActivity(), { address ->
            if (address != null) {
                val splitAddress = address.split(", ")
                binding.thoroughfareTv.text = splitAddress[0]
                binding.localityTv.text = splitAddress[1]
            } else {
                binding.thoroughfareTv.text = "Unreachable"
                binding.localityTv.text = "Unreachable"
            }
        })

        groupList.add("Main\nCategory")
        groupList.add("Merchants")

        viewModel.queryHomeData()
        observeLiveData()

        binding.homeRv.layoutManager = LinearLayoutManager(requireContext())
        binding.homeRv.adapter = homeAdapter


        binding.homeRetryButton.setOnClickListener {
            viewModel.queryHomeData()
            observeLiveData()
        }

        val bottomSheetBehavior = BottomSheetBehavior.from(binding.locationBottomSheet.root)

        binding.locationCv.setOnClickListener {
            if (!isBottomSheetActive) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                binding.darkBg.visibility = View.VISIBLE
                isBottomSheetActive = true
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                binding.darkBg.visibility = View.GONE
                isBottomSheetActive = false
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.darkBg.visibility = View.GONE
                        isBottomSheetActive = false
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.darkBg.visibility = View.VISIBLE
                        isBottomSheetActive = true
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        binding.darkBg.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                isBottomSheetActive = false
            }

        })

        binding.darkBg.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            binding.darkBg.visibility = View.GONE
            isBottomSheetActive = false
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
}