package com.vroomvroom.android.view.ui.orders

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.data.model.order.Status
import com.vroomvroom.android.databinding.FragmentOrderBinding
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.view.resource.Resource
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.orders.adapter.OrderAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class OrderFragment : BaseFragment<FragmentOrderBinding>(
    FragmentOrderBinding::inflate
) {

    private val orderAdapter by lazy { OrderAdapter() }
    private lateinit var status: Status

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        status = arguments?.getParcelable(STATUS) ?: Status.PENDING
        binding.ordersRv.adapter = orderAdapter
        observeOrdersByStatusLiveData()
        observeIsRefreshed()
        ordersViewModel.getOrdersByStatus(status)

        orderAdapter.onMerchantClicked = { merchant ->
            if (merchant.id.isNotBlank()) {
                findNavController().safeNavigate(
                    OrdersFragmentDirections.actionGlobalToMerchantFragment(merchant.id)
                )
            }
        }
        orderAdapter.onOrderClicked = { orderId ->
            findNavController().navigate(
                OrdersFragmentDirections.actionOrdersFragmentToOrderDetailFragment(orderId)
            )
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            ordersViewModel.getOrdersByStatus(status)
            mainActivityViewModel.isRefreshed.postValue(true)
        }
    }

    private fun observeOrdersByStatusLiveData() {
        ordersViewModel.orders.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Loading -> {
                    binding.ordersRv.visibility = View.GONE
                    binding.commonNoticeLayout.hideNotice()
                    binding.shimmerLayout.startShimmer()
                    binding.shimmerLayout.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    val orders = response.data
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
                is Resource.Error -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.ordersRv.visibility = View.GONE
                    binding.commonNoticeLayout.showNetworkError {
                        ordersViewModel.getOrdersByStatus(status) }
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun observeIsRefreshed() {
        mainActivityViewModel.isRefreshed.observe(viewLifecycleOwner) { refreshed ->
            if (refreshed) {
                ordersViewModel.getOrdersByStatus(status)
            }
        }
    }

    companion object {
        private const val STATUS = "status"

        /**
         * current won't use anymore
         */
        fun newInstance(
            status: Status
        ): OrderFragment {
            val fragment = OrderFragment()
            val bundle = Bundle()
            bundle.putParcelable(STATUS, status)
            fragment.arguments = bundle
            return fragment
        }
    }
}