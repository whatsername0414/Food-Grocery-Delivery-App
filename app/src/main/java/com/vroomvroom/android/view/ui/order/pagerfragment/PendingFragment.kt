package com.vroomvroom.android.view.ui.order.pagerfragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.FragmentPendingBinding

class PendingFragment : Fragment() {

    private lateinit var binding: FragmentPendingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPendingBinding.inflate(inflater)
        return binding.root
    }

}