package com.example.glamora.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

// Use AndroidViewModel to safely access the application context
class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    // --- State ---
    // These are the reactive properties the UI will observe.
    var name = mutableStateOf("")
    var email = mutableStateOf("")
    var profilePhoto = mutableStateOf<Bitmap?>(null)

    // --- Constants for Local Storage ---
    private val prefsName = "ProfilePrefs"
    private val keyName = "profile_name"
    private val keyEmail = "profile_email"
    private val photoFileName = "profile_photo.png"

    // --- Name and Email Persistence ---

    /**
     * Saves the user's name and email to SharedPreferences.
     */
    fun saveNameEmailToLocal(name: String, email: String) {
        val context = getApplication<Application>().applicationContext
        val sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(keyName, name)
            putString(keyEmail, email)
            apply()
        }
    }

    /**
     * Loads the user's name and email from SharedPreferences and updates the ViewModel state.
     */
    fun loadNameEmailFromLocal() {
        val context = getApplication<Application>().applicationContext
        val sharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        name.value = sharedPreferences.getString(keyName, "Your Name") ?: "Your Name"
        email.value = sharedPreferences.getString(keyEmail, "your.email@example.com") ?: "your.email@example.com"
    }


    // --- Profile Photo Persistence ---

    /**
     * Saves the provided Bitmap image to the app's internal storage.
     */
    fun savePhotoToLocal(bitmap: Bitmap) {
        val context = getApplication<Application>().applicationContext
        try {
            val file = File(context.filesDir, photoFileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle error, e.g., show a toast
        }
    }

    /**
     * Loads the profile photo from internal storage and updates the ViewModel state.
     */
    fun loadPhotoFromLocal() {
        val context = getApplication<Application>().applicationContext
        try {
            val file = File(context.filesDir, photoFileName)
            if (file.exists()) {
                FileInputStream(file).use { stream ->
                    profilePhoto.value = BitmapFactory.decodeStream(stream)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle error
        }
    }
}
