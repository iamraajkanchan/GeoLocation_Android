package com.example.geolocation_android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LocationBroadcast(iLocation: ILocation) : BroadcastReceiver() {
    private var iLocation: ILocation? = iLocation

    override fun onReceive(context: Context?, intent: Intent?) {
        println("LocationService :: LocationBroadcast :: broadcast received")
        if (intent?.action?.equals(LOCATION_SERVICE_INTENT) == true) {
            iLocation?.getLocationLatitudeAndLongitude(
                intent.getStringExtra(LOCATION_LATITUDE).toString(),
                intent.getStringExtra(LOCATION_LONGITUDE).toString()
            )
        }
    }
}