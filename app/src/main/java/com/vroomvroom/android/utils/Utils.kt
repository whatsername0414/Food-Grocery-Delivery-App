package com.vroomvroom.android.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.SystemClock
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.vmadalin.easypermissions.EasyPermissions
import java.io.IOException

class SafeClickListener(
    private var defaultInterval: Int = 3000,
    private val onSafeCLick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeCLick(v)
    }
}

object Utils {

    fun <A : Activity> Activity.startNewActivity(activity: Class<A>) {
        Intent(this, activity).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
        }
    }

    fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    fun Activity.hideSoftKeyboard() {
        currentFocus?.let {
            val inputMethodManager = ContextCompat.getSystemService(this, InputMethodManager::class.java)!!
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    fun clearFocus(view: View, editText: TextInputEditText, activity: Activity) {
        if (view !is TextInputEditText) {
            view.setOnTouchListener { _, _ ->
                activity.hideSoftKeyboard()
                editText.clearFocus()
                editText.isCursorVisible = false
                false
            }
        }
        if (view is TextInputEditText) {
            view.setOnTouchListener { _, _ ->
                false
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                clearFocus(innerView, editText, activity)
            }
        }
    }

    fun hasLocationPermission(context: Context) = EasyPermissions.hasPermissions(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION)

    fun requestLocationPermission(hostFragment: Fragment) {
        EasyPermissions.requestPermissions(
            hostFragment,
            "You need to accept location permissions for this app to properly work.",
            Constants.PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    fun createLocationRequest(activity: Activity, hostFragment: Fragment) {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val taskVerifyLocationSetting: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        taskVerifyLocationSetting.addOnSuccessListener { locationSettingsResponse ->
            if (locationSettingsResponse.locationSettingsStates?.isLocationUsable == true) {
                requestLocationPermission(hostFragment)
            }
        }

        taskVerifyLocationSetting.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(activity,
                        Constants.REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    fun initLocation(location: Any): LatLng? {
        val coordinates = when (location) {
            is String -> {
                val stringCoordinates = location.split(", ")
                LatLng(stringCoordinates[0].toDouble(), stringCoordinates[1].toDouble())
            }
            is Location -> {
                LatLng(location.latitude, location.longitude)
            }
            else -> null
        }

        coordinates?.let { latLng ->
            return latLng
        }
        return null
    }

    fun customGeoCoder(coordinates: LatLng, context: Context): Address? {
        val geoCoder = Geocoder(context)
        try {
            val addresses = geoCoder.getFromLocation(
                coordinates.latitude,
                coordinates.longitude,
                1
            )
            if (addresses.isNotEmpty()) {
                return addresses.first()
            }
        } catch (e: IOException) {
            Toast.makeText(context, "Unknown error occurred", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
        return null
    }

    fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        val safeClickListener = SafeClickListener {
            onSafeClick(it)
        }
        setOnClickListener(safeClickListener)
    }
}