package com.example.glamora.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glamora.R
import com.example.glamora.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(navController: NavController) {
    val colorScheme = MaterialTheme.colorScheme
    val productName = stringResource(R.string.top_product_name)
    val productPrice = 49.00
    val productOriginalPrice = 99.00
    val productImageResId = R.drawable.dress1
    val productDescription = stringResource(R.string.product_description_short)
    val productCategory = stringResource(R.string.product_category_fashion)

    val sizes = listOf("S", "M", "L", "XL", "XXL")
    val colors = listOf(Color(0xFFEFE8E1), Color.Black, Color.Red, colorScheme.secondary)

    var quantity by remember { mutableStateOf(1) }
    var selectedSize by remember { mutableStateOf(sizes[1]) }
    var selectedColor by remember { mutableStateOf(colors[0]) }
    var readMore by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colorScheme.background,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { navController.navigate("cart") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.add_to_cart),
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.85f)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color(0xFFFFEBEE)) // Light pink background behind image
                ) {
                    Image(
                        painter = painterResource(id = productImageResId),
                        contentDescription = productName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(32.dp)),
                        contentScale = ContentScale.Crop
                    )

                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(16.dp)
                            .background(colorScheme.surface.copy(alpha = 0.9f), CircleShape)
                            .size(44.dp)
                            .border(2.dp, colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_desc),
                            tint = colorScheme.primary
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp)
                        .clip(RoundedCornerShape(32.dp)),
                    color = colorScheme.surface,
                    shadowElevation = 0.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = productName,
                                style = Typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                                modifier = Modifier.weight(1f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colorScheme.primary.copy(alpha = 0.3f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(18.dp))
                                Text("4.8", style = Typography.labelLarge, modifier = Modifier.padding(start = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = productCategory,
                            style = Typography.bodyMedium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Price", style = Typography.labelMedium)
                                Row {
                                    Text(
                                        "$%.2f".format(productPrice),
                                        style = Typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                        color = colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "$%.2f".format(productOriginalPrice),
                                        style = Typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough),
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(colorScheme.secondary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp)
                            ) {
                                IconButton(onClick = { if (quantity > 1) quantity-- }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = colorScheme.primary)
                                }
                                Text("$quantity", style = Typography.bodyLarge)
                                IconButton(onClick = { quantity++ }) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = colorScheme.primary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = if (readMore) productDescription else productDescription.take(100) + "...",
                            style = Typography.bodyLarge.copy(lineHeight = 24.sp),
                            modifier = Modifier.clickable { readMore = !readMore }
                        )
                        Text(
                            text = if (readMore) stringResource(R.string.show_less) else stringResource(R.string.read_more),
                            style = Typography.bodyMedium.copy(color = colorScheme.primary, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.clickable { readMore = !readMore }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.offer_text),
                                style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Select Size", style = Typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            sizes.forEach { size ->
                                FilterChip(
                                    selected = selectedSize == size,
                                    onClick = { selectedSize = size },
                                    label = { Text(size) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colorScheme.primary,
                                        selectedLabelColor = colorScheme.onPrimary,
                                        containerColor = colorScheme.primary.copy(alpha = 0.15f),
                                        labelColor = colorScheme.onBackground
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("Select Color", style = Typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            colors.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (selectedColor == color) 3.dp else 1.dp,
                                            color = if (selectedColor == color) colorScheme.primary else Color.Gray,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = color }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
