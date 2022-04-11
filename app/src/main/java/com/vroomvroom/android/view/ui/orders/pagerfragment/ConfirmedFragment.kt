package com.vroomvroom.android.view.ui.orders.pagerfragment

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.databinding.FragmentConfirmedBinding
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.orders.OrdersFragmentDirections
import com.vroomvroom.android.view.ui.orders.adapter.OrderAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ConfirmedFragment : BaseFragment<FragmentConfirmedBinding>(
    FragmentConfirmedBinding::inflate
) {

    private val orderAdapter by lazy { OrderAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ordersRv.adapter = orderAdapter
        observeOrdersByStatusLiveData()
        ordersViewModel.queryOrdersByStatus("Confirmed")

        orderAdapter.onMerchantClicked = { merchant ->
            if (merchant._id.isNotBlank()) {
                findNavController().safeNavigate(
                    OrdersFragmentDirections.actionGlobalToMerchantFragment(merchant._id)
                )
            }
        }
        orderAdapter.onOrderClicked = { orderId ->
            findNavController().navigate(
                OrdersFragmentDirections.actionOrdersFragmentToOrderDetailFragment(orderId)
            )
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            ordersViewModel.queryOrdersByStatus("Confirmed")
            mainActivityViewModel.isRefreshed.postValue(true)
        }

    }

    private fun observeOrdersByStatusLiveData() {
        ordersViewModel.ordersByStatus.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ViewState.Loading -> {
                    binding.ordersRv.visibility = View.GONE
                    binding.commonNoticeLayout.hideNotice()
                    binding.shimmerLayout.startShimmer()
                    binding.shimmerLayout.visibility = View.VISIBLE
                }
                is ViewState.Success -> {
                    val orders = response.result
                    if (orders.isEmpty()) {
                        orderAdapter.submitList(emptyList())
                        binding.commonNoticeLayout.showEmptyOrder {
                            findNavController().popBackStack() }
                    } else {
                        orderAdapter.submitList(orders)
                        binding.ordersRv.visibility = View.VISIBLE
                    }
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = false
                }
                is ViewState.Error -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.ordersRv.visibility = View.GONE
                    binding.commonNoticeLayout.showNetworkError {
                        ordersViewModel.queryOrdersByStatus("Confirmed") }
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

}