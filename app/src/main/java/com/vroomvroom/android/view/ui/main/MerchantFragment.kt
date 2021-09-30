package com.vroomvroom.android.view.ui.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.vroomvroom.android.MerchantQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentMerchantBinding
import com.vroomvroom.android.view.adapter.ProductsAdapter
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MerchantFragment : Fragment() {

    private val viewModel by viewModels<DataViewModel>()
    private val productsAdapter by lazy { ProductsAdapter() }
    private var isUserScrolling: Boolean = false
    private val args: MerchantFragmentArgs by navArgs()
    private lateinit var binding: FragmentMerchantBinding
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMerchantBinding.inflate(inflater)
        linearLayoutManager = binding.byCategoryRv.layoutManager as LinearLayoutManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ctlMerchant.setExpandedTitleColor(ContextCompat.getColor(requireContext(), R.color.white))
        viewModel.queryMerchant(args.id)

        //private functions
        observeMerchantLiveData()
        syncTabWithRecyclerView()

        binding.byCategoryRv.adapter = productsAdapter

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeMerchantLiveData() {
        viewModel.merchant.observe(viewLifecycleOwner) { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.connectionError.visibility = View.GONE
                    binding.merchantFetchProgress.visibility = View.VISIBLE
                }
                is ViewState.Success -> {
                    if (response.value?.data == null) {
                        binding.connectionError.visibility = View.VISIBLE
                    } else {
                        binding.connectionError.visibility = View.GONE
                    }
                    val merchant = response.value?.data?.getMerchant
                    productsAdapter.submitList(merchant?.products)
                    initializeTabItem(merchant?.products)
                    viewBinder(merchant)
                    binding.merchantFetchProgress.visibility = View.GONE
                }
                is ViewState.Error -> {
                    binding.merchantFetchProgress.visibility = View.GONE
                    binding.connectionError.visibility = View.VISIBLE
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun viewBinder(merchant: MerchantQuery.GetMerchant?) {
        val ratingCount = merchant?.ratingCount
        val rating = if (ratingCount!! < 2) {
                "$ratingCount rating"
            } else "$ratingCount ratings"
        binding.ctlMerchant.title = merchant.name
        binding.closingTv.text = "Open until ${merchant.closing} PM"
        binding.merchantRating.text = "${merchant.rating}($rating)"
        Glide.with(binding.merchantImg)
            .load(merchant.img_url)
            .into(binding.merchantImg)
    }

    private fun initializeTabItem(products: List<MerchantQuery.Product?>?) {
        products?.forEach { product ->
            binding.tlMerchant.addTab(binding.tlMerchant.newTab().setText(product?.name))
        }
    }

    private fun syncTabWithRecyclerView() {
        // Move recyclerview to the position selected by user
        binding.tlMerchant.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (!isUserScrolling) {
                    val position = tab.position
                    smoothScroller(position)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {
            }
            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        // Detect recyclerview position and select tab respectively.
        binding.byCategoryRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isUserScrolling = true
                }  else if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    isUserScrolling = false
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isUserScrolling) {
                    val firstCompletePos = linearLayoutManager.findFirstVisibleItemPosition()

                    if (firstCompletePos != binding.tlMerchant.selectedTabPosition)
                        binding.tlMerchant.getTabAt(firstCompletePos)?.select()
                }
            }
        })
    }

    private fun smoothScroller(position: Int) {
        val smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        smoothScroller.targetPosition = position
        linearLayoutManager.startSmoothScroll(smoothScroller)
    }
}