package com.example.glamora.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.glamora.R
import com.example.glamora.ui.navigation.Screen
import com.example.glamora.ui.theme.Typography
import com.example.glamora.ui.component.BottomBar
import com.example.glamora.viewmodel.ProfileViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    auth: FirebaseAuth,
    profileViewModel: ProfileViewModel = viewModel() // This receives the ViewModel instance
) {
    val context = LocalContext.current

    // --- START: INVITE FRIENDS LOGIC ---

    // Launcher for picking a contact. It receives the contact's URI.
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri ->
        if (contactUri != null) {
            // A contact was selected. Query for the display name.
            context.contentResolver.query(contactUri, arrayOf(ContactsContract.Contacts.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val contactName = cursor.getString(0)
                    Toast.makeText(context, "You selected $contactName", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "No contact selected.", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher for requesting READ_CONTACTS permission.
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // If permission was just granted, launch the contact picker.
            contactPickerLauncher.launch(null)
        } else {
            Toast.makeText(context, "Read Contacts permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- END: INVITE FRIENDS LOGIC ---

    LaunchedEffect(key1 = true) {
        profileViewModel.loadNameEmailFromLocal()
        profileViewModel.loadPhotoFromLocal()
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_screen_title_v2),
                        style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_desc),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = { BottomBar(navController) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { navController.navigate(Screen.CameraSensor.route) }
            ) {
                profileViewModel.profilePhoto.value?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.profile_image_desc_v2),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = stringResource(R.string.profile_image_desc_v2),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .size(32.dp)
                        .clickable { navController.navigate(Screen.CameraSensor.route) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_profile_icon_desc_v2),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = profileViewModel.name.value.ifEmpty { "Your Name" },
                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = profileViewModel.email.value.ifEmpty { "your.email@example.com" },
                style = Typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = 8.dp)
            ) {
                ProfileMenuItem(
                    icon = Icons.Default.PersonOutline,
                    label = stringResource(R.string.menu_edit_profile_v2),
                    onClick = { navController.navigate(Screen.CameraSensor.route) }
                )
                ProfileMenuItem(
                    icon = Icons.Default.CreditCard,
                    label = stringResource(R.string.menu_payment_method_v2),
                    onClick = { /* TODO */ }
                )
                ProfileMenuItem(
                    icon = Icons.Default.LocationOn,
                    label = stringResource(R.string.menu_address_v2),
                    onClick = { navController.navigate(Screen.Address.route) }
                )
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    label = stringResource(R.string.menu_order_history_v2),
                    onClick = { navController.navigate("order_history") }
                )
                ProfileMenuItem(
                    icon = Icons.Default.PeopleOutline, // Using a more appropriate icon
                    label = stringResource(R.string.menu_invite_friends_v2),
                    onClick = {
                        // This logic runs when the user clicks "Invite Friends"
                        when (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_CONTACTS
                        )) {
                            PackageManager.PERMISSION_GRANTED -> {
                                // Permission is already granted, so launch the picker.
                                contactPickerLauncher.launch(null)
                            }
                            else -> {
                                // Permission is not granted, so request it.
                                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            }
                        }
                    }
                )
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    label = stringResource(R.string.menu_help_center_v2),
                    onClick = { /* TODO */ }
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    label = stringResource(R.string.menu_logout),
                    onClick = {
                        auth.signOut()
                        googleSignInClient.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    textColor = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = Typography.bodyLarge,
                color = textColor
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = stringResource(R.string.arrow_forward_icon_desc_v2),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}
