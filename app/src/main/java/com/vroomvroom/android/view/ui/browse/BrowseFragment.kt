package com.vroomvroom.android.view.ui.browse

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.databinding.FragmentBrowseBinding
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.browse.adapter.BrowseCategoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class BrowseFragment : BaseFragment<FragmentBrowseBinding>(
    FragmentBrowseBinding::inflate
) {

    private val adapter by lazy { BrowseCategoryAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.categoryRv.adapter = adapter

        if (mainViewModel.categories.value == null) {
            mainViewModel.queryCategory("search")
        }
        observeBrowseCategory()

        binding.searchBar.setOnClickListener {
            findNavController().navigate(
                BrowseFragmentDirections.actionBrowseFragmentToMerchantSearchFragment(null)
            )
        }

        adapter.onCategoryClicked = { searchTerm ->
            findNavController().navigate(
                BrowseFragmentDirections.actionBrowseFragmentToMerchantSearchFragment(searchTerm)
            )
        }

    }

    private fun observeBrowseCategory() {
        mainViewModel.categories.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.apply {
                        binding.commonNoticeLayout.hideNotice()
                        title.visibility = View.GONE
                        categoryRv.visibility = View.GONE
                        categoryShimmerLayout.apply {
                            visibility = View.VISIBLE
                            startShimmer()
                        }
                    }
                }
                is ViewState.Success -> {
                    val category = response.result.getCategories
                    adapter.submitList(category)
                    binding.apply {
                        title.visibility = View.VISIBLE
                        categoryRv.visibility = View.VISIBLE
                        categoryShimmerLayout.apply {
                            stopShimmer()
                            visibility = View.GONE
                        }
                    }
                }
                is ViewState.Error -> {
                    binding.apply {
                        categoryRv.visibility = View.GONE
                        title.visibility = View.GONE
                        categoryShimmerLayout.apply {
                            stopShimmer()
                            visibility = View.GONE
                        }
                        binding.commonNoticeLayout.showNetworkError {
                            mainViewModel.queryCategory("search")
                        }
                    }
                }
            }
        }
    }

}