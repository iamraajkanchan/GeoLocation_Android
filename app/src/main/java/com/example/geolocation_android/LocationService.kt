package com.example.geolocation_android

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*

const val LOCATION_LATITUDE: String = "location latitude"
const val LOCATION_LONGITUDE: String = "location longitude"

class LocationService : Service() {

    private lateinit var locationBinder: LocationBinder
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onBind(intent: Intent?): IBinder {
        return locationBinder
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        locationBinder = LocationBinder()
        fusedLocationProviderClient = FusedLocationProviderClient(applicationContext)
        getLastLocation()
    }

    private fun getLastLocation() {
        if (askLocationPermissionsDenied()) {
            enforceCallingOrSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                "Fine Location Permission Denied from the user!!!"
            )
            enforceCallingOrSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                "Coarse Location Permission Denied from the user!!!"
            )
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            if (it.result != null) {
                configureSendBroadcastMessage(it.result)
            } else {
                getNewLocationData()
            }
        }
    }

    private fun getNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 50000
            numUpdates = 1
            fastestInterval = 50000
            smallestDisplacement = 170f // 170m = 0.1 mile
        }
        if (askLocationPermissionsDenied()) {
            enforceCallingOrSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
                "Fine Location Permission Denied from the user!!!"
            )
            enforceCallingOrSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                "Coarse Location Permission Denied from the user!!!"
            )
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    configureSendBroadcastMessage(locationResult.lastLocation)
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    super.onLocationAvailability(locationAvailability)
                }
            },
            Looper.getMainLooper()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun askLocationPermissionsDenied() : Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun configureSendBroadcastMessage(location: Location) {
        val locationIntent = Intent(LOCATION_SERVICE_INTENT).apply {
            putExtra(LOCATION_LATITUDE, location.latitude)
            putExtra(LOCATION_LONGITUDE, location.longitude)
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(locationIntent)
    }

    inner class LocationBinder : Binder() {
        fun getLocationService(): LocationService = LocationService()
    }
}
