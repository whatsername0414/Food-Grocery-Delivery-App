package com.vroomvroom.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vroomvroom.android.view.ui.home.HomeActivity
import com.vroomvroom.android.utils.Utils.startNewActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTheme(R.style.Theme_VroomVroom)
        startNewActivity(HomeActivity::class.java)

    }
}