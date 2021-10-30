package com.vroomvroom.android.view.ui.main

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ActivityHomeBinding
import com.vroomvroom.android.viewmodel.AuthViewModel
import com.vroomvroom.android.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class HomeActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onStart() {
        super.onStart()
        authViewModel.getCurrentUser()
        authViewModel.getIdToken()
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

        val currentFragment = navController.currentDestination?.id
        val locationFragment = R.id.locationFragment

        mainViewModel.location.observe(this, {
            if (it == null) {
                if (currentFragment == locationFragment) {
                    navController.navigate(locationFragment)
                } else navController.navigate(R.id.action_homeFragment_to_locationFragment)
            }
        })

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.searchFragment, R.id.orderFragment, R.id.profileFragment -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
                else -> bottomNavigationView.visibility = View.GONE
            }
        }
    }
}