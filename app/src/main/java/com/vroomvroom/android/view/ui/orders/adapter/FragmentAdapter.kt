package com.vroomvroom.android.view.ui.orders.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vroomvroom.android.data.model.order.Status
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
                return OrderFragment.newInstance(Status.CONFIRMED)
            }
            2 -> {
                return OrderFragment.newInstance(Status.TO_RECEIVE)
            }
            3 -> {
                return OrderFragment.newInstance(Status.DELIVERED)
            }
            4 -> {
                return OrderFragment.newInstance(Status.CANCELLED)
            }
        }
        return  OrderFragment.newInstance(Status.PENDING)
    }
}