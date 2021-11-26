package com.vroomvroom.android.view.ui.main

import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ActivityHomeBinding
import com.vroomvroom.android.viewmodel.AuthViewModel
import com.vroomvroom.android.viewmodel.LocationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class HomeActivity : AppCompatActivity() {

    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onStart() {
        super.onStart()
        authViewModel.saveIdToken()
        authViewModel.getIdToken()
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(authViewModel.broadcastReceiver, intentFilter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bottomNavigationView = binding.bottomNavigationView
        val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
        navController = navHostFragment.findNavController()
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.searchFragment, R.id.orderFragment, R.id.profileFragment -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
                else -> bottomNavigationView.visibility = View.GONE
            }
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(authViewModel.broadcastReceiver)
    }
}