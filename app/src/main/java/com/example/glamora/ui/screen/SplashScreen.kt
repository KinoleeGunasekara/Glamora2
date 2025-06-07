// File: ui/screen/SplashScreen.kt
package com.example.glamora.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.example.glamora.R // Ensure this R exists for drawable resources
import com.example.glamora.ui.navigation.Screen // Corrected: Added the import for your navigation Screen object

@Composable
fun SplashScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Main background color for the entire screen
    ) {
        // --- Full-screen Image as the Base Layer ---
        // This image will fill the entire screen, acting as the background.
        Image(
            painter = painterResource(id = R.drawable.splash_screen_img), // Your main splash image resource
            contentDescription = stringResource(id = R.string.splash_image_content_description),
            contentScale = ContentScale.Crop, // Crop to fill bounds while maintaining aspect ratio
            modifier = Modifier.fillMaxSize() // Image fills the entire parent Box
        )

        // --- Bottom Section: Texts and Button Overlay ---
        // This Column is placed on top of the image and aligned to the bottom.
        // It will have rounded top corners visible against the image.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter) // Align this column to the bottom of the Box
                .fillMaxHeight(0.35f) // Adjusted height to make the text section smaller (e.g., 35% from bottom)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)) // Apply rounded corners to the top
                .background(MaterialTheme.colorScheme.surface) // Background for the bottom content section (white in the example)
                .padding(horizontal = 24.dp, vertical = 24.dp), // Added vertical padding for better spacing within this section
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly // Distribute children and space evenly within the column
        ) {
            // "Dress Like Never Before" text
            Text(
                text = stringResource(id = R.string.splash_tagline),
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            // "Where Every Click is a Step Closer to Your Perfect Purchase !" sub-headline
            Text(
                text = stringResource(id = R.string.splash_subtext),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )

            // "Swipe To Get Started" button
            Button(
                onClick = {
                    navController.navigate(
                        route = Screen.Login.route,
                        navOptions = navOptions {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f) // Occupy 90% of the bottom section's width
                    .height(56.dp) // Fixed height for the button
                    .clip(RoundedCornerShape(28.dp)), // Fully rounded capsule shape
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) // Use primary color for button background
            ) {
                Text(
                    text = stringResource(id = R.string.splash_button_text),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
