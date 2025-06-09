package com.example.glamora.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.glamora.R
import com.example.glamora.ui.component.GlamoraTextField
import com.example.glamora.ui.navigation.Screen
import com.example.glamora.ui.theme.Typography
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp

    val horizontalPadding = when {
        screenWidthDp > 700 -> 128.dp
        isLandscape -> 64.dp
        else -> 24.dp
    }
    val verticalPadding = if (isLandscape) 24.dp else 48.dp

    val scrollState = rememberScrollState()

    // Fade overlay animatable alpha state
    val fadeOverlayAlpha = remember { androidx.compose.animation.core.Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    // Launch fade out animation once on first composition
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            fadeOverlayAlpha.animateTo(0f, animationSpec = androidx.compose.animation.core.tween(durationMillis = 400))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Your existing background image and overlay
        Image(
            painter = painterResource(id = R.drawable.background_img),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Your existing UI content here (no changes)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                Text(
                    text = stringResource(R.string.hello),
                    style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.welcome_message),
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.login),
                        style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    GlamoraTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = stringResource(R.string.email),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    GlamoraTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(R.string.password),
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.login),
                            style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.new_user),
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { navController.navigate(Screen.Register.route) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }

        // *Fade overlay on top of everything*
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = fadeOverlayAlpha.value))
        )
    }
}