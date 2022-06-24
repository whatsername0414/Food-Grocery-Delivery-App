package com.vroomvroom.android.view.ui.orders.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vroomvroom.android.utils.Constants.CANCELLED
import com.vroomvroom.android.utils.Constants.CONFIRMED
import com.vroomvroom.android.utils.Constants.DELIVERED
import com.vroomvroom.android.utils.Constants.PENDING
import com.vroomvroom.android.utils.Constants.TO_RECEIVE
import com.vroomvroom.android.view.ui.orders.OrderFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class FragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            1 -> {
                return OrderFragment.newInstance(CONFIRMED)
            }
            2 -> {
                return OrderFragment.newInstance(TO_RECEIVE)
            }
            3 -> {
                return OrderFragment.newInstance(DELIVERED)
            }
            4 -> {
                return OrderFragment.newInstance(CANCELLED)
            }
        }
        return  OrderFragment.newInstance(PENDING)
    }
}