package com.example.glamora.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.glamora.R
import com.example.glamora.local.SavedCardEntity
import com.example.glamora.viewmodel.CartViewModel

/**
 * PaymentMethodsScreen displays all user's saved payment methods.
 * Allows users to view masked card details, set default cards, and delete saved cards.
 * Shows appropriate card type icons and maintains security by only displaying last 4 digits.
 * Integrates with existing CartViewModel to minimize code complexity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodsScreen(
    navController: NavController,
    cartViewModel: CartViewModel
) {
    // Observe saved cards from CartViewModel's StateFlow
    val savedCards by cartViewModel.savedCards.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Methods") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Handle empty state when user hasn't saved any cards yet
            if (savedCards.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                "No saved cards yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Cards will appear here after you save them during checkout",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            } else {
                // Display each saved card with management options
                items(savedCards) { card ->
                    SavedCardItem(
                        card = card,
                        onSetDefault = { cartViewModel.setDefaultCard(card.cardId) },
                        onDelete = { cartViewModel.deleteCard(card.cardId) }
                    )
                }
            }
        }
    }
}

/**
 * SavedCardItem displays individual saved payment method card.
 * Shows card type icon, masked card number (only last 4 digits), holder name, and expiry.
 * Provides actions to set as default or delete the card with confirmation dialog.
 * Security: Never displays full card number or CVV information.
 */
@Composable
fun SavedCardItem(
    card: SavedCardEntity,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Map card type to appropriate drawable resource for visual identification
    val cardIcon = when (card.cardType) {
        "VISA" -> R.drawable.img_visa_logo
        "MASTERCARD" -> R.drawable.img_mastercard_logo
        "RUPAY" -> R.drawable.img_rupay_logo
        else -> R.drawable.img_mastercard_logo // Default fallback icon
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left side: Card icon and details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Card type logo for easy visual identification
                    Image(
                        painter = painterResource(id = cardIcon),
                        contentDescription = card.cardType,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        // Masked card number showing only last 4 digits for security
                        Text(
                            text = "**** **** **** ${card.lastFourDigits}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = card.cardHolderName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Expires ${card.expiryDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Right side: Action buttons in vertical layout
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (card.isDefault) {
                        // Default card indicator - no action needed
                        AssistChip(
                            onClick = { }, // Disabled click for default indicator
                            label = { Text("Default") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    } else {
                        // Set as default button for non-default cards
                        TextButton(onClick = onSetDefault) {
                            Text("Set Default")
                        }
                    }

                    // Delete button - always available with confirmation
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }

    // Delete confirmation dialog to prevent accidental deletion
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Card") },
            text = {
                Text("Are you sure you want to delete this payment method? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete() // Execute deletion
                        showDeleteDialog = false
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
