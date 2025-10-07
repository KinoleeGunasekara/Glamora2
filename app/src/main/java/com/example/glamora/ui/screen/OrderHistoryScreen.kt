package com.example.glamora.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.glamora.local.OrderEntity
import com.example.glamora.local.OrderItemEntity
import com.example.glamora.viewmodel.CartViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * OrderHistoryScreen displays all user orders in a scrollable list.
 * Each order can be expanded to show individual items purchased.
 * Reuses existing CartViewModel to minimize code complexity.
 * Implements Material 3 design system with proper theming and navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    navController: NavController,
    cartViewModel: CartViewModel
) {
    // Observe state changes from existing CartViewModel
    val orders by cartViewModel.orders.collectAsState()
    val orderItems by cartViewModel.orderItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order History") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Handle empty state when user has no orders yet
            if (orders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "No orders yet",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your order history will appear here after your first purchase",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Display orders (newest first due to ORDER BY orderDate DESC in DAO)
                items(orders) { order ->
                    OrderCard(
                        order = order,
                        orderItems = orderItems[order.orderId] ?: emptyList(),
                        onLoadItems = { cartViewModel.loadOrderItems(order.orderId) }
                    )
                }
            }
        }
    }
}

/**
 * OrderCard displays a single order with expandable item details.
 * Implements lazy loading - items are only fetched when user expands the card.
 * Uses Material 3 Card component with elevation and rounded corners.
 */
@Composable
fun OrderCard(
    order: OrderEntity,
    orderItems: List<OrderItemEntity>,
    onLoadItems: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Automatically load items when card is first displayed (lazy loading)
    LaunchedEffect(order.orderId) {
        if (orderItems.isEmpty()) {
            onLoadItems()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Order header displaying key information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Order number and date
                Column {
                    Text(
                        text = order.orderNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatDate(order.orderDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Right side: Total amount and status
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", order.totalAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = order.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Toggle button for expanding/collapsing item details
            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isExpanded) "Hide Items"
                    else "View Items (${orderItems.size})"
                )
            }

            // Expandable section showing individual order items
            if (isExpanded && orderItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // Display each item in the order
                orderItems.forEach { item ->
                    OrderItemRow(item = item)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * OrderItemRow displays a single product within an order.
 * Shows product image, name, quantity, and total price for that item.
 * Uses AsyncImage for efficient image loading with Coil library.
 */
@Composable
fun OrderItemRow(item: OrderItemEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Product thumbnail using Coil for image loading
        AsyncImage(
            model = item.image,
            contentDescription = item.title,
            modifier = Modifier
                .size(50.dp)
                .padding(end = 8.dp),
            contentScale = ContentScale.Crop
        )

        // Product details: name and quantity
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Qty: ${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Total price for this item (price × quantity)
        Text(
            text = "$${String.format("%.2f", item.price * item.quantity)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Formats Date object to human-readable string.
 * Used for displaying order timestamps in a consistent format across the app.
 * Uses locale-aware formatting for international support.
 */
private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
    return formatter.format(date)
}
