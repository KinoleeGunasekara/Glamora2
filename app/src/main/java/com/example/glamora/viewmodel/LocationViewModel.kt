package com.example.glamora.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.location.Location
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import java.io.File
import java.io.IOException

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    // State to hold the location string, e.g., "Lat: 12.34, Lon: 56.78"
    val locationDisplay = mutableStateOf("Fetching location...")
    private val addressFileName = "address.txt"

    /**
     * Fetches the last known device location.
     * This is fast but might be stale or null.
     */
    @SuppressLint("MissingPermission") // Permissions are checked in the Composable
    fun fetchLocation() {
        val context = getApplication<Application>().applicationContext
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val locationTask: Task<Location> = fusedLocationClient.lastLocation
        locationTask.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val newLocation = "Lat: ${location.latitude}, Lon: ${location.longitude}"
                locationDisplay.value = newLocation
                saveLocationToLocal(newLocation) // Save the new location
            } else {
                locationDisplay.value = "Location not available. Please enable GPS."
            }
        }
        locationTask.addOnFailureListener {
            locationDisplay.value = "Failed to get location."
        }
    }

    /**
     * Saves the location string to a local file named address.txt.
     */
    private fun saveLocationToLocal(location: String) {
        val context = getApplication<Application>().applicationContext
        try {
            val file = File(context.filesDir, addressFileName)
            file.writeText(location)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Loads the last saved location from address.txt.
     */
    fun loadLocationFromLocal() {
        val context = getApplication<Application>().applicationContext
        try {
            val file = File(context.filesDir, addressFileName)
            if (file.exists()) {
                locationDisplay.value = file.readText()
            } else {
                locationDisplay.value = "No saved address."
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
