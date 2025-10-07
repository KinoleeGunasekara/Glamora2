package com.example.glamora.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.glamora.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraSensorScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel
) {
    val context = LocalContext.current

    // Load all profile data when the screen is first displayed.
    LaunchedEffect(Unit) {
        profileViewModel.loadNameEmailFromLocal()
        profileViewModel.loadPhotoFromLocal()
    }

    // Local state for text fields, initialized from the ViewModel.
    // This prevents recomposition on every key press from re-triggering network/db calls.
    var name by remember { mutableStateOf(TextFieldValue(profileViewModel.name.value)) }
    var email by remember { mutableStateOf(TextFieldValue(profileViewModel.email.value)) }

    // This effect ensures that if the ViewModel's data is loaded *after* the initial
    // composition, the text fields are updated accordingly.
    LaunchedEffect(profileViewModel.name.value, profileViewModel.email.value) {
        name = TextFieldValue(profileViewModel.name.value)
        email = TextFieldValue(profileViewModel.email.value)
    }

    // --- Camera Launcher ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            profileViewModel.profilePhoto.value = bitmap // Immediately update UI
            Toast.makeText(context, "Photo captured! Press Save to keep it.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Permission Launcher ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(null as Void?) // Relaunch camera if permission is granted
        } else {
            Toast.makeText(context, "Camera permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable {
                        when (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        )) {
                            PackageManager.PERMISSION_GRANTED -> cameraLauncher.launch(null as Void?)
                            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                profileViewModel.profilePhoto.value?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop, // Crop to fill the circle shape
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Text("Tap to add photo", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = {
                // 1. Update ViewModel states from the local UI state
                profileViewModel.name.value = name.text
                profileViewModel.email.value = email.text

                // 2. Persist all data using ViewModel functions
                profileViewModel.saveNameEmailToLocal(name.text, email.text)
                profileViewModel.profilePhoto.value?.let { newPhoto ->
                    profileViewModel.savePhotoToLocal(newPhoto)
                }

                // 3. Show confirmation and navigate back
                Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Changes")
            }
        }
    }
}
