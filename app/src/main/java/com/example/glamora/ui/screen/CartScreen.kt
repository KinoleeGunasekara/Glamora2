package com.example.glamora.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glamora.R
import com.example.glamora.ui.component.BottomBar
import com.example.glamora.ui.navigation.Screen

data class CartItemModel(
    val id: Int,
    val name: String,
    val price: Double,
    val imageResId: Int
)

val mockCartItems = listOf(
    CartItemModel(1, "Black Cherry Midi Dress", 65.0, R.drawable.dress2),
    CartItemModel(2, "Burgundy Velvet Heels", 89.0, R.drawable.heels1)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: androidx.navigation.NavController) {
    val subtotal = mockCartItems.sumOf { it.price }
    val taxes = 10.00
    val delivery = 8.00
    val total = subtotal + taxes + delivery

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
                        // Navigate back to Discover (or your details screen)
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

                items(mockCartItems) { item ->
                    CartItem(
                        product = item,
                        quantity = 1, // fixed quantity
                        onQuantityChange = {} // no-op
                    )
                }

                item {
                    TextButton(
                        onClick = { /* no-op */ },
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

            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp)
            ) {
                SummaryRow(stringResource(R.string.subtotal), "$%.2f".format(subtotal))
                SummaryRow(stringResource(R.string.taxes_and_fees), "$%.2f".format(taxes))
                SummaryRow(stringResource(R.string.delivery), "$%.2f".format(delivery))
                Divider(Modifier.padding(vertical = 16.dp))
                SummaryRow(stringResource(R.string.total), "$%.2f".format(total), isTotal = true)

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { /* no-op */ },
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

@Composable
fun CartItem(
    product: CartItemModel,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(product.imageResId),
            contentDescription = product.name,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(16.dp))

        Column(Modifier.weight(1f)) {
            Text(product.name, fontWeight = FontWeight.SemiBold)
            Text("$%.2f".format(product.price), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                QuantityButton("-", onClick = { /* no-op */ })
                Text("$quantity", Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                QuantityButton("+", onClick = { /* no-op */ })
            }
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
            containerColor = if (symbol == "+") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
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
