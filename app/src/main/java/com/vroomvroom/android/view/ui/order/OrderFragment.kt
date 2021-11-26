package com.vroomvroom.android.view.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.vroomvroom.android.databinding.FragmentOrderBinding
import com.vroomvroom.android.view.ui.order.pagerfragment.FragmentAdapter

class OrderFragment : Fragment() {

    private lateinit var binding: FragmentOrderBinding
    private lateinit var fragmentAdapter: FragmentAdapter
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderBinding.inflate(inflater)
        navController = findNavController()
        val fragmentManager = requireActivity().supportFragmentManager
        fragmentAdapter = FragmentAdapter(fragmentManager, lifecycle)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.orderToolbar.setupWithNavController(navController, appBarConfiguration)

        binding.orderVp.adapter = fragmentAdapter
        binding.orderTl.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let {
                    binding.orderVp.currentItem = it
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

        binding.orderVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                binding.orderTl.getTabAt(position)?.select()

            }
        })

    }
}