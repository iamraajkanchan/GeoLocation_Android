package com.example.geolocation_android

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity()
{
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private val PERMISSION_ID = 44
    override fun onCreate(savedInstanceState : Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        getLastLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation()
    {
        if (checkPermissions())
        {
            if (isLocationEnable())
            {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                    val location = it.result
                    if (location == null)
                    {
                        requestNewLocationData()
                    } else
                    {
                        tvLatitude.text = location.latitude.toString()
                        tvLongitude.text = location.longitude.toString()
                    }
                }
            } else
            {
                Toast.makeText(this , "Please turn on your location" , Toast.LENGTH_LONG).show()
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                    startActivity(this)
                }
            }
        } else
        {
            requestPermissions()
        }

    }

    private fun checkPermissions() : Boolean
    {
        return ActivityCompat.checkSelfPermission(
            this , android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this , android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnable() : Boolean
    {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(
        requestCode : Int , permissions : Array<out String> , grantResults : IntArray
    )
    {
        super.onRequestPermissionsResult(requestCode , permissions , grantResults)
        if (requestCode == PERMISSION_ID)
        {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                getLastLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData()
    {
        val locationRequest = LocationRequest()
        locationRequest.interval = 50000
        locationRequest.fastestInterval = 50000
        locationRequest.smallestDisplacement = 170f // 170m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest , locationCallback , Looper.myLooper() !!
        )
    }

    private val locationCallback = object : LocationCallback()
    {
        override fun onLocationResult(locationResult : LocationResult)
        {
            val location = locationResult.lastLocation
            tvLatitude.text = location.latitude.toString()
            tvLongitude.text = location.longitude.toString()
        }

        override fun onLocationAvailability(locationAvailability : LocationAvailability)
        {
            super.onLocationAvailability(locationAvailability)
        }
    }

    private fun requestPermissions()
    {
        ActivityCompat.requestPermissions(
            this , arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION ,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) , PERMISSION_ID
        )
    }
}