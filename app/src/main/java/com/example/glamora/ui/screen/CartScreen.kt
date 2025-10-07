package com.example.glamora.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.glamora.R
import com.example.glamora.local.CartItemEntity
import com.example.glamora.ui.component.BottomBar
import com.example.glamora.ui.component.ProductImageLoader // Use the shared image loader
import com.example.glamora.ui.navigation.Screen
import com.example.glamora.viewmodel.CartViewModel // Import the new ViewModel

// NOTE: mockCartItems and CartItemModel are now removed, as data comes from CartItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    cartViewModel: CartViewModel // Inject CartViewModel
) {
    // REQUIREMENT: Read data from a local data source
    // Collect the data stream (Flow) from the Room Database
    val cartItems by cartViewModel.cartItems.collectAsState()
    val subtotal by cartViewModel.cartTotal.collectAsState()

    // Taxes and delivery are hardcoded as per your original logic
    val taxes = 10.00
    val delivery = 8.00
    val total = subtotal + taxes + delivery

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.cart_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Discover.route) {
                            popUpTo(Screen.Discover.route) { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomBar(navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (cartItems.isEmpty()) {
            // Show empty cart message when no items are collected from Room
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.cart_empty),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else if (isLandscape) {
            // Landscape Layout
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Iterate through the live data from Room
                    items(cartItems, key = { it.productId }) { item ->
                        CartItem(
                            product = item,
                            onQuantityChange = { change ->
                                cartViewModel.updateQuantity(item, change)
                            },
                            onRemove = {
                                cartViewModel.removeItem(item)
                            }
                        )
                    }

                    item {
                        TextButton(
                            onClick = {
                                navController.navigate(Screen.Discover.route) {
                                    popUpTo(Screen.Discover.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.add_more_items),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .width(300.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp)
                ) {
                    item {
                        SummaryRow(stringResource(R.string.subtotal), "$%.2f".format(subtotal))
                        SummaryRow(stringResource(R.string.taxes_and_fees), "$%.2f".format(taxes))
                        SummaryRow(stringResource(R.string.delivery), "$%.2f".format(delivery))
                        Divider(Modifier.padding(vertical = 16.dp))
                        SummaryRow(stringResource(R.string.total), "$%.2f".format(total), isTotal = true)

                        Spacer(Modifier.height(24.dp))
                    }

                    item {
                        Button(
                            // UPDATED: Navigate to the Payment Screen
                            onClick = { navController.navigate(Screen.Payment.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(stringResource(R.string.checkout), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        } else {
            // Portrait Layout
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    // Iterate through the live data from Room
                    items(cartItems, key = { it.productId }) { item ->
                        CartItem(
                            product = item,
                            onQuantityChange = { change ->
                                cartViewModel.updateQuantity(item, change)
                            },
                            onRemove = {
                                cartViewModel.removeItem(item)
                            }
                        )
                    }

                    item {
                        TextButton(
                            onClick = {
                                navController.navigate(Screen.Discover.route) {
                                    popUpTo(Screen.Discover.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.add_more_items),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp)
                ) {
                    item {
                        SummaryRow(stringResource(R.string.subtotal), "$%.2f".format(subtotal))
                        SummaryRow(stringResource(R.string.taxes_and_fees), "$%.2f".format(taxes))
                        SummaryRow(stringResource(R.string.delivery), "$%.2f".format(delivery))
                        Divider(Modifier.padding(vertical = 16.dp))
                        SummaryRow(stringResource(R.string.total), "$%.2f".format(total), isTotal = true)

                        Spacer(Modifier.height(24.dp))
                    }

                    item {
                        Button(
                            // UPDATED: Navigate to the Payment Screen
                            onClick = { navController.navigate(Screen.Payment.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Text(stringResource(R.string.checkout), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// CartItem Composable (Uses CartItemEntity and calls ViewModel logic)
// ----------------------------------------------------------------------------------

@Composable
fun CartItem(
    product: CartItemEntity, // Now uses the Room Entity
    onQuantityChange: (change: Int) -> Unit, // +1 for increase, -1 for decrease
    onRemove: () -> Unit // Function to remove the item completely
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Use the robust ProductImageLoader component
        ProductImageLoader(
            imageUrl = product.image,
            contentDescription = product.title,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Text(product.title, fontWeight = FontWeight.SemiBold)
            Text("$%.2f".format(product.price), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Decrease Quantity Button: Calls onQuantityChange(-1)
                QuantityButton(
                    symbol = "-",
                    onClick = { onQuantityChange(-1) }
                )
                Text(
                    text = "${product.quantity}",
                    modifier = Modifier.padding(horizontal = 8.dp),
                    fontWeight = FontWeight.Bold
                )
                // Increase Quantity Button: Calls onQuantityChange(1)
                QuantityButton(
                    symbol = "+",
                    onClick = { onQuantityChange(1) }
                )
            }
        }

        // Add a small remove/delete button
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(40.dp) // Increased from 24.dp to 40.dp
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = "Remove Item",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp) // Increased from default to make icon more visible
            )
        }
    }
}

@Composable
fun QuantityButton(symbol: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(symbol, color = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
fun SummaryRow(label: String, value: String, isTotal: Boolean = false) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
