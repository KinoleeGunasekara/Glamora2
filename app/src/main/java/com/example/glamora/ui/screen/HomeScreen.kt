package com.example.glamora.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.glamora.R
import com.example.glamora.data.sampleProducts
import com.example.glamora.ui.component.BottomBar
import com.example.glamora.ui.component.ProductCard
import com.example.glamora.ui.navigation.Screen
@Composable
fun HomeScreen(navController: NavController) {
    val categories = listOf("Tops", "Dresses", "Bags", "Shoes", "Jewelry")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var searchQuery by remember { mutableStateOf("") }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp

    val horizontalPadding = when {
        screenWidthDp > 840 -> 48.dp
        screenWidthDp > 600 -> 32.dp
        else -> 16.dp
    }

    val gridColumns = when {
        screenWidthDp > 1000 -> 5
        screenWidthDp > 840 -> 4
        screenWidthDp > 600 -> 3
        isLandscape -> 3
        else -> 2
    }

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        if (isLandscape) {
            // you LANDSCAPE MODE is here: the banner is split and the content is in the right scrollable
            Row(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Banner on the left
                Image(
                    painter = painterResource(id = R.drawable.banner),
                    contentDescription = "Promotional Banner",
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = 200.dp, max = 300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    contentScale = ContentScale.Crop
                )

                // Scrollable content on the right
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(horizontal = horizontalPadding)
                ) {
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Search Bar
                        Surface(
                            shape = RoundedCornerShape(50),
                            tonalElevation = 4.dp,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp
                                    ),
                                    decorationBox = { innerTextField ->
                                        Box {
                                            if (searchQuery.isEmpty()) {
                                                Text(
                                                    text = "Search products",
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 16.sp
                                                )
                                            }
                                            innerTextField()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories.size) { index ->
                                val category = categories[index]
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(text = category) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = if (selectedCategory == category)
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surface,
                                        labelColor = if (selectedCategory == category)
                                            MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Header Row
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Popular Products 🔥",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "See All",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable {
                                    navController.navigate(Screen.Discover.route)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Grid outside scrollable column for performance
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 80.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(sampleProducts) { product ->
                            ProductCard(
                                product = product,
                                onClick = {
                                    navController.navigate(Screen.ProductDetail.route)
                                },
                                onCartClick = {
                                    navController.navigate(Screen.Cart.route)
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // this is your PORTRAIT MODE: your existing layout by the column
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(horizontal = horizontalPadding)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Bar
                    Surface(
                        shape = RoundedCornerShape(50),
                        tonalElevation = 4.dp,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                ),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                text = "Search products",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories.size) { index ->
                            val category = categories[index]
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(text = category) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = if (selectedCategory == category)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surface,
                                    labelColor = if (selectedCategory == category)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Banner
                    Image(
                        painter = painterResource(id = R.drawable.banner),
                        contentDescription = "Promotional Banner",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Header Row
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Popular Products 🔥",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "See All",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Discover.route)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Grid outside scrollable column for performance
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridColumns),
                    contentPadding = PaddingValues(
                        start = horizontalPadding,
                        end = horizontalPadding,
                        top = 8.dp,
                        bottom = 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(sampleProducts) { product ->
                        ProductCard(
                            product = product,
                            onClick = {
                                navController.navigate(Screen.ProductDetail.route)
                            },
                            onCartClick = {
                                navController.navigate(Screen.Cart.route)
                            }
                        )
                    }
                }
            }
        }
    }
}