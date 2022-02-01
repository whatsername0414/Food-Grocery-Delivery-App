package com.vroomvroom.android.view.ui.orders.pagerfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vroomvroom.android.databinding.FragmentConfirmedBinding
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.activityviewmodel.ActivityViewModel
import com.vroomvroom.android.view.ui.orders.OrdersFragmentDirections
import com.vroomvroom.android.view.ui.orders.adapter.OrderAdapter
import com.vroomvroom.android.view.ui.orders.viewmodel.OrdersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ConfirmedFragment : Fragment() {

    private val viewModel by viewModels<OrdersViewModel>()
    private val activityViewModel by activityViewModels<ActivityViewModel>()
    private val orderAdapter by lazy { OrderAdapter() }

    private lateinit var binding: FragmentConfirmedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConfirmedBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ordersRv.adapter = orderAdapter
        observeOrdersByStatusLiveData()
        viewModel.queryOrdersByStatus("Confirmed")

        orderAdapter.onMerchantClicked = { merchant ->
            if (merchant._id.isNotBlank()) {
                findNavController().navigate(
                    OrdersFragmentDirections.actionOrdersFragmentToMerchantFragment(merchant._id)
                )
            }
        }
        orderAdapter.onOrderClicked = { orderId ->
            findNavController().navigate(
                OrdersFragmentDirections.actionOrdersFragmentToOrderDetailFragment(orderId)
            )
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.queryOrdersByStatus("Confirmed")
            activityViewModel.isRefreshed.postValue(true)
        }

        binding.btnRetry.setOnClickListener {
            viewModel.queryOrdersByStatus("Confirmed")
        }
        binding.btnStartShopping.setOnClickListener {
            findNavController().popBackStack()
        }

    }

    private fun observeOrdersByStatusLiveData() {
        viewModel.ordersByStatus.observe(viewLifecycleOwner, { response ->
            when(response) {
                is ViewState.Loading -> {
                    binding.ordersRv.visibility = View.GONE
                    binding.emptyOrderLayout.visibility = View.GONE
                    binding.shimmerLayout.startShimmer()
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.connectionFailedLayout.visibility = View.GONE
                }
                is ViewState.Success -> {
                    val orders = response.result.getOrdersByStatus
                    if (orders.isEmpty()) {
                        orderAdapter.submitList(emptyList())
                        binding.emptyOrderLayout.visibility = View.VISIBLE
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.visibility = View.GONE
                    } else {
                        orderAdapter.submitList(orders)
                        binding.shimmerLayout.stopShimmer()
                        binding.emptyOrderLayout.visibility = View.GONE
                        binding.shimmerLayout.visibility = View.GONE
                        binding.ordersRv.visibility = View.VISIBLE
                    }
                    binding.swipeRefreshLayout.isRefreshing = false
                }
                is ViewState.Error -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.ordersRv.visibility = View.GONE
                    binding.emptyOrderLayout.visibility = View.GONE
                    binding.connectionFailedLayout.visibility = View.VISIBLE
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        })
    }

}