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
import com.vroomvroom.android.databinding.FragmentHomeBinding
import com.vroomvroom.android.view.adapter.HomeAdapter
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment: Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel by viewModels<DataViewModel>()
    private val groupList: MutableList<String> = mutableListOf()
    private val homeAdapter by lazy { HomeAdapter(requireContext(), groupList) }

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

        groupList.add("Main\nCategory")
        groupList.add("Merchants")

        binding.homeConnectionFailedNotice.visibility = View.GONE

        viewModel.queryHomeData()
        observeLiveData()

        binding.homeRv.layoutManager = LinearLayoutManager(requireContext())
        binding.homeRv.adapter = homeAdapter

        binding.homeRetryButton.setOnClickListener {
            viewModel.queryHomeData()
            observeLiveData()
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