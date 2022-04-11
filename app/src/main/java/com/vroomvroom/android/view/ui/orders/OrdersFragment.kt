package com.vroomvroom.android.view.ui.orders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.vroomvroom.android.OrdersStatusQuery
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentOrdersBinding
import com.vroomvroom.android.utils.Constants.CHANNEL_ID
import com.vroomvroom.android.utils.Constants.CHANNEL_NAME
import com.vroomvroom.android.view.state.ViewState
import com.vroomvroom.android.view.ui.activityviewmodel.ActivityViewModel
import com.vroomvroom.android.view.ui.auth.viewmodel.AuthViewModel
import com.vroomvroom.android.view.ui.orders.adapter.FragmentAdapter
import com.vroomvroom.android.view.ui.orders.viewmodel.OrdersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class OrdersFragment : Fragment() {

    private val ordersViewModel by viewModels<OrdersViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()
    private val activityViewModel by activityViewModels<ActivityViewModel>()

    @Inject lateinit var builder: NotificationCompat.Builder
    private lateinit var binding: FragmentOrdersBinding
    private lateinit var fragmentAdapter: FragmentAdapter
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersBinding.inflate(inflater)
        navController = findNavController()
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentAdapter = FragmentAdapter(fragmentManager, lifecycle)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        observeUser()
        observeOrdersStatusLiveData()
        observeRefreshed()
        createNotificationChannel()

    }

    private fun setupViewPager() {
        binding.viewPager.apply {
            adapter = fragmentAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.tabLayout.getTabAt(position)?.select()
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
        authViewModel.userRecord.observe(viewLifecycleOwner, { users ->
            if (!users.isNullOrEmpty()) {
                binding.tabLayout.visibility = View.VISIBLE
                binding.viewPager.visibility = View.VISIBLE
                ordersViewModel.queryOrdersStatus()
                setupViewPager()
                setupTabLayout()
            } else {
                binding.tabLayout.visibility = View.GONE
                binding.viewPager.visibility = View.GONE
                navController.navigate(R.id.action_ordersFragment_to_authBottomSheetFragment)
            }
        })
    }

    private fun observeOrdersStatusLiveData() {
        ordersViewModel.ordersStatus.observe(viewLifecycleOwner, { response ->
            when (response) {
                is ViewState.Loading -> Unit
                is ViewState.Success -> {
                    val ordersStatus = response.result.getOrdersStatus
                    filterStatus(ordersStatus)
                }
                is ViewState.Error -> Unit
            }
        })
    }

    private fun observeRefreshed() {
        activityViewModel.isRefreshed.observe(viewLifecycleOwner, { refreshed ->
            if (refreshed) {
                ordersViewModel.queryOrdersStatus()
            }
        })
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
                            createNotification(order.status, order.id)
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
                            createNotification(order.status, order.id)
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
                            createNotification(order.status, order.id)
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

    private fun createNotification(status: String, orderId: String) {
        val notificationId = (0..1000).random()
        when (status) {
            "Confirmed" -> {
                setupNotification(
                    getString(R.string.confirmed_notification_title),
                    getString(R.string.confirmed_notification_content),
                    notificationId
                )
            }
            "To Receive" -> {
                setupNotification(
                    getString(R.string.to_receive_notification_title),
                    getString(R.string.to_receive_notification_content),
                    notificationId
                )
            }
        }
        ordersViewModel.mutationUpdateOrderNotified(orderId)
    }

    private fun setupNotification(title: String, content: String, id: Int) {
        val notification = builder
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(requireContext()).notify(id, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lightColor = Color.GREEN
                enableLights(true)
            }
            val manager = requireActivity().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}