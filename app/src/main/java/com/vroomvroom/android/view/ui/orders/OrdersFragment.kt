package com.vroomvroom.android.view.ui.orders

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.vroomvroom.android.OrdersStatusQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentOrdersBinding
import com.vroomvroom.android.utils.Constants.CONFIRMED
import com.vroomvroom.android.utils.Constants.CONFIRMED_TAB_POSITION
import com.vroomvroom.android.utils.Constants.TO_RECEIVE
import com.vroomvroom.android.utils.Constants.TO_RECEIVE_TAB_POSITION
import com.vroomvroom.android.utils.NotificationManager
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.home.HomeActivity
import com.vroomvroom.android.view.ui.orders.adapter.FragmentAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class OrdersFragment : BaseFragment<FragmentOrdersBinding>(
    FragmentOrdersBinding::inflate
) {

    @Inject lateinit var notificationManager: NotificationManager
    private lateinit var fragmentAdapter: FragmentAdapter
    private val args by navArgs<OrdersFragmentArgs>()

    private var type: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentManager = requireActivity().supportFragmentManager
        fragmentAdapter = FragmentAdapter(fragmentManager, lifecycle)

        observeUser()
        observeOrdersStatusLiveData()
        observeRefreshed()
        notificationManager.createNotificationChannel()

        binding.cart.setOnClickListener {
            findNavController().safeNavigate(
                OrderDetailFragmentDirections.actionGlobalToCartBottomSheetFragment()
            )
        }
        type = args.status

    }

    private fun setupViewPager() {
        binding.viewPager.apply {
            adapter = fragmentAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                        when (type) {
                            CONFIRMED -> {
                                binding.tabLayout.getTabAt(CONFIRMED_TAB_POSITION)?.select()
                            }
                            TO_RECEIVE -> {
                                binding.tabLayout.getTabAt(TO_RECEIVE_TAB_POSITION)?.select()
                            }
                            else -> {
                                binding.tabLayout.getTabAt(position)?.select()
                            }
                        }
                    type = null

                }
            })
            (getChildAt(0) as? RecyclerView)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let {
                    binding.viewPager.currentItem = it
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeUser() {
        authViewModel.userRecord.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tabLayout.visibility = View.VISIBLE
                binding.viewPager.visibility = View.VISIBLE
                ordersViewModel.queryOrdersStatus()
                setupViewPager()
                setupTabLayout()
            } else {
                binding.tabLayout.visibility = View.GONE
                binding.viewPager.visibility = View.GONE
                findNavController().navigate(R.id.action_ordersFragment_to_authBottomSheetFragment)
            }
        }
    }

    private fun observeOrdersStatusLiveData() {
        ordersViewModel.ordersStatus.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ViewState.Loading -> Unit
                is ViewState.Success -> {
                    val ordersStatus = response.result.getOrdersStatus
                    filterStatus(ordersStatus)
                }
                is ViewState.Error -> Unit
            }
        }
    }

    private fun observeRefreshed() {
        mainActivityViewModel.isRefreshed.observe(viewLifecycleOwner) { refreshed ->
            if (refreshed) {
                ordersViewModel.queryOrdersStatus()
            }
        }
    }

    private fun filterStatus(ordersStatus: OrdersStatusQuery.GetOrdersStatus?) {
        ordersStatus?.let { orders ->
            val pending = orders.pending
            pending?.let {
                setupTabBadge(it.size, 0)
            }
            val confirmed = orders.confirmed
            confirmed?.let {
                setupTabBadge(it.size, 1)
                it.forEach { item ->
                    item?.let { order ->
                        if (!order.notified) {
                            notificationManager
                                .createNotification(order.status, HomeActivity::class.java)
                            ordersViewModel.mutationUpdateOrderNotified(order.id)
                        }
                    }
                }
            }
            val toReceive = orders.to_receive
            toReceive?.let {
                setupTabBadge(it.size, 2)
                it.forEach { item ->
                    item?.let { order ->
                        if (!order.notified) {
                            notificationManager
                                .createNotification(order.status, HomeActivity::class.java)
                            ordersViewModel.mutationUpdateOrderNotified(order.id)
                        }
                    }
                }
            }
            val delivered = orders.delivered
            delivered?.let {
                setupTabBadge(it.size, 3)
                it.forEach { item ->
                    item?.let { order ->
                        if (!order.notified) {
                            notificationManager
                                .createNotification(order.status, HomeActivity::class.java)
                            ordersViewModel.mutationUpdateOrderNotified(order.id)
                        }
                    }
                }
            }
            val cancelled = orders.cancelled
            cancelled?.let {
                setupTabBadge(it.size, 4)
            }
        }
    }

    private fun setupTabBadge(number: Int, position: Int) {
        if (number > 0) {
            binding.tabLayout.getTabAt(position)?.orCreateBadge?.number = number
        } else {
            binding.tabLayout.getTabAt(position)?.removeBadge()
        }
    }
}