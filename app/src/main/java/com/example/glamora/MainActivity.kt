package com.example.glamora // Make sure this matches your package name

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.glamora.ui.navigation.GlamoraNavGraph // Import your NavGraph
import com.example.glamora.ui.theme.GlamoraTheme // Import your theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GlamoraTheme { // Apply your custom theme
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GlamoraNavGraph() // Call your navigation graph here
                }
            }
        }
    }
}