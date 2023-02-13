package com.example.geolocation_android

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*

const val LOCATION_LATITUDE: String = "location latitude"
const val LOCATION_LONGITUDE: String = "location longitude"

class LocationService : Service() {

    private var locationBinder: LocationBinder? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var latitude: String = ""
    var longitude: String = ""

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            configureSendBroadcastMessage(location)
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
        val locationRequest = LocationRequest.create()
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
                configureSendBroadcastMessage(location)
            }
        }
    }

    private fun configureSendBroadcastMessage(location: Location?) {
        val locationIntent = Intent(LOCATION_SERVICE_INTENT).apply {
            putExtra(LOCATION_LATITUDE, location?.latitude.toString())
            putExtra(LOCATION_LONGITUDE, location?.longitude.toString())
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(locationIntent)
    }

    inner class LocationBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }
}