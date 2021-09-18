package com.vroomvroom.android.view.ui.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.ActivityHomeBinding
import com.vroomvroom.android.repository.UserPreferences
import com.vroomvroom.android.view.ui.startNewActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userPreferences = UserPreferences(this)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val bottomNavigationView = findViewById<BottomNavigationView>(binding.bottomNavigationView.id)
        val navHostFragment = supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
        bottomNavigationView.setupWithNavController(navHostFragment.navController)

        val inflater = navHostFragment.navController.navInflater
        val graph = inflater.inflate(R.navigation.nav_main)


        userPreferences.location.asLiveData().observe(this, {
            if (it != null) {
                graph.startDestination = R.id.merchantFragment
            } else {
                binding.bottomNavigationView.visibility = View.GONE
                graph.startDestination = R.id.locationFragment
            }

            navHostFragment.navController.graph = graph
        })
    }
}