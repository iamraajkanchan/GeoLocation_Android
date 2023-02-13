package com.example.geolocation_android

import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

private const val PERMISSION_ID = 44
const val LOCATION_SERVICE_INTENT = "location service intent"

class MainActivity : AppCompatActivity() {

    private var locationService: LocationService? = null
    private var isServiceConnected: Boolean = false

    private val locationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (name?.className?.equals("LocationService") == true) {
                val binder = service as LocationService.LocationBinder
                locationService = binder.getService()
                isServiceConnected = true
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            if (name?.className?.equals("LocationService") == true) {
                locationService = null
                isServiceConnected = false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                Intent(this@MainActivity, LocationService::class.java).apply {
                    bindService(this, locationServiceConnection, BIND_AUTO_CREATE)
                }
            }
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                /*
                tvLatitude.text = locationService?.latitude
                tvLongitude.text = locationService?.longitude
                */
                LocalBroadcastManager.getInstance(this).registerReceiver(
                    LocationBroadcastReceiver(),
                    IntentFilter(LOCATION_SERVICE_INTENT)
                )
            }
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_ID
        )
        /*Note: these permissions must be declared in AndroidManifest.xml file*/
    }

    override fun onStop() {
        super.onStop()
        unbindService(locationServiceConnection)
        isServiceConnected = false
    }

    inner class LocationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action?.equals(LOCATION_SERVICE_INTENT) == true) {
                tvLatitude.text = intent.getStringExtra(LOCATION_LATITUDE)
                tvLongitude.text = intent.getStringExtra(LOCATION_LONGITUDE)
            } else {
                Snackbar.make(
                    coordinatorMain, "Latitude and Longitude Not found!!!", Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}