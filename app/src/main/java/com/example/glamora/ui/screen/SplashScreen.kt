package com.example.glamora.ui.screen

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.example.glamora.R
import com.example.glamora.ui.navigation.Screen
import kotlinx.coroutines.launch
@Composable
fun SplashScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val swipeThreshold = 50f
    val offsetY = remember { Animatable(0f) }
    val fadeAlpha = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    var fullScreen by remember { mutableStateOf(false) }

    fun onSwipeComplete() {
        navController.navigate(
            route = Screen.Login.route,
            navOptions = navOptions {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .alpha(fadeAlpha.value)
    ) {
        if (!isLandscape) {
            // Portrait mode: background image
            Image(
                painter = painterResource(id = R.drawable.splash_screen_img),
                contentDescription = stringResource(id = R.string.splash_image_content_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Draggable bottom sheet with "Dress like..." text and swipe hint
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, offsetY.value.toInt()) }
                    .fillMaxHeight(if (fullScreen) 1f else 0.35f)  // reduced from 0.45f to 0.35f
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (-offsetY.value > swipeThreshold) {
                                    fullScreen = true
                                    coroutineScope.launch {
                                        launch { fadeAlpha.animateTo(0f, tween(250)) }
                                        offsetY.animateTo(-offsetY.value, tween(300))
                                        onSwipeComplete()
                                    }
                                } else {
                                    coroutineScope.launch {
                                        offsetY.animateTo(0f, tween(250))
                                    }
                                }
                            },
                            onVerticalDrag = { _, dragAmount ->
                                val newOffset = (offsetY.value + dragAmount).coerceAtMost(0f)
                                coroutineScope.launch { offsetY.snapTo(newOffset) }
                            }
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Dress like a Star",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 65.dp)
                    )
                    Text(
                        text = "Explore the latest trends and elevate your style with Glamora's curated fashion picks.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .offset(y = (-44).dp) // moves text 4dp up
                    )
                }

                // Swipe up hint
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "Swipe Up",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Simple arrow icon for swipe hint
                    Icon(
                        painter = painterResource(id = R.drawable.arrow), // Use your arrow icon here
                        contentDescription = "Swipe up icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        } else {
            // Landscape mode: image on left, text on right
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_screen_img),
                    contentDescription = stringResource(id = R.string.splash_image_content_description),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 32.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Text(
                        text = stringResource(id = R.string.splash_tagline),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = stringResource(id = R.string.splash_subtext),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                    Button(
                        onClick = {
                            // In landscape, just navigate directly with no animation
                            onSwipeComplete()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE91E63),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Get Started",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}