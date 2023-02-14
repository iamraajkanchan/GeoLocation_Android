package com.example.geolocation_android

/**
 * Note: This interface is not used in the project because we have used LocalBroadcastManager.
 * */

interface ILocation {
    fun getLocationLatitudeAndLongitude(latitude: String, longitude: String)
}