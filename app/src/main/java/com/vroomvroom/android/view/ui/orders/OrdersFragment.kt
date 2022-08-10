package com.vroomvroom.android.view.ui.orders

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentOrdersBinding
import com.vroomvroom.android.utils.Constants.CONFIRMED
import com.vroomvroom.android.utils.Constants.CONFIRMED_TAB_POSITION
import com.vroomvroom.android.utils.Constants.TO_RECEIVE
import com.vroomvroom.android.utils.Constants.TO_RECEIVE_TAB_POSITION
import com.vroomvroom.android.utils.Utils.safeNavigate
import com.vroomvroom.android.view.ui.base.BaseFragment
import com.vroomvroom.android.view.ui.orders.adapter.FragmentAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class OrdersFragment : BaseFragment<FragmentOrdersBinding>(
    FragmentOrdersBinding::inflate
) {


    private lateinit var fragmentAdapter: FragmentAdapter
    private val args by navArgs<OrdersFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentManager = requireActivity().supportFragmentManager
        fragmentAdapter = FragmentAdapter(fragmentManager, lifecycle)

        if (user != null) {
            binding.tabLayout.visibility = View.VISIBLE
            binding.viewPager.visibility = View.VISIBLE
            setupViewPager()
            setupTabLayout()
        } else {
            binding.tabLayout.visibility = View.GONE
            binding.viewPager.visibility = View.GONE
            findNavController().navigate(R.id.action_ordersFragment_to_authBottomSheetFragment)
        }

        binding.cart.setOnClickListener {
            findNavController().safeNavigate(
                OrderDetailFragmentDirections.actionGlobalToCartBottomSheetFragment()
            )
        }

    }

    private fun setupViewPager() {
        binding.viewPager.apply {
            adapter = fragmentAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                        when (args.status) {
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

    private fun setupTabBadge(number: Int, position: Int) {
        if (number > 0) {
            binding.tabLayout.getTabAt(position)?.orCreateBadge?.number = number
        } else {
            binding.tabLayout.getTabAt(position)?.removeBadge()
        }
    }
}