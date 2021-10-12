package com.vroomvroom.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vroomvroom.android.view.ui.main.HomeActivity
import com.vroomvroom.android.view.ui.Utils.startNewActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val userPreferences = UserPreferences(this)
//
//        userPreferences.token.asLiveData().observe(this, {
//            val activity = if (it == null) AuthActivity::class.java else HomeActivity::class.java
//            startNewActivity(activity)
//        })
        startNewActivity(HomeActivity::class.java)

    }
}