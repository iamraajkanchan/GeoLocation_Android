package com.example.geolocation_android

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import com.google.android.gms.location.*

class LocationService : Service() {

    private var locationMessenger: Messenger? = null
    private var locationBinder: LocationBinder? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var latitude: String = ""
    var longitude: String = ""

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            latitude = location.latitude.toString()
            longitude = location.longitude.toString()
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            super.onLocationAvailability(locationAvailability)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return locationBinder
    }

    override fun onCreate() {
        super.onCreate()
        locationBinder = LocationBinder()
        fusedLocationProviderClient = FusedLocationProviderClient(applicationContext)
        getLastLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationBinder = null; fusedLocationProviderClient = null; locationMessenger = null
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.interval = 50000
        locationRequest.fastestInterval = 50000
        locationRequest.smallestDisplacement = 170f // 170m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient?.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()!!
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationProviderClient?.lastLocation?.addOnCompleteListener {
            val location = it.result
            if (location == null) {
                requestNewLocationData()
            } else {
                latitude = location.latitude.toString()
                longitude = location.longitude.toString()
                Toast.makeText(
                    applicationContext,
                    "Latitude : $latitude, Longitude: $longitude",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    inner class LocationBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }
}