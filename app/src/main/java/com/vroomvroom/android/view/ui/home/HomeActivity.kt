package com.vroomvroom.android.view.ui.home

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
import com.vroomvroom.android.view.ui.auth.viewmodel.AuthViewModel
import com.vroomvroom.android.view.ui.base.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class HomeActivity : AppCompatActivity() {

    private val authViewModel by viewModels<AuthViewModel>()
    private val mainActivityViewModel by viewModels<MainViewModel>()

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onStart() {
        super.onStart()
        authViewModel.saveIdToken()
        authViewModel.getLocalIdToken()
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(authViewModel.broadcastReceiver, intentFilter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bottomNavigationView = binding.bottomNavigationView
        val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
        navController = navHostFragment.findNavController()
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.browseFragment, R.id.ordersFragment, R.id.accountFragment -> {
                    mainActivityViewModel.isBottomBarVisible = true
                    bottomNavigationView.visibility = View.VISIBLE
                }
                else -> {
                    mainActivityViewModel.isBottomBarVisible = false
                    bottomNavigationView.visibility = View.GONE
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(authViewModel.broadcastReceiver)
    }
}