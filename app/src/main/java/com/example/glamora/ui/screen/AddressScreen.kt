package com.example.glamora.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.glamora.viewmodel.LocationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressScreen(
    navController: NavController,
    locationViewModel: LocationViewModel = viewModel()
) {
    val context = LocalContext.current
    val locationText by locationViewModel.locationDisplay

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, fetch location
            locationViewModel.fetchLocation()
        } else {
            // Permission denied
            locationViewModel.locationDisplay.value = "Location permission denied."
        }
    }

    // Load saved location or request new one on screen launch
    LaunchedEffect(Unit) {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> {
                locationViewModel.fetchLocation()
            }
            else -> {
                // If no permission, try loading a previously saved location first
                locationViewModel.loadLocationFromLocal()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Location") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Current GPS Coordinates:",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = locationText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                // Check for permission and launch request if needed
                when (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    PackageManager.PERMISSION_GRANTED -> {
                        locationViewModel.fetchLocation()
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            }) {
                Text("Refresh Location")
            }
        }
    }
}
