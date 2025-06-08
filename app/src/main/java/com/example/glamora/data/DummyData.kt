package com.example.glamora.data

import com.example.glamora.R

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageResId: Int,
    val description: String,
    val category: String // new field to support filtering
)

val sampleProducts = listOf(
    // Women
    Product(
        id = "w1",
        name = "Elegant Pink Dress",
        price = 59.99,
        imageResId = R.drawable.dress1,
        description = "A beautiful flowing pink dress for all occasions.",
        category = "Women"
    ),
    Product(
        id = "w2",
        name = "Floral Summer Dress",
        price = 49.99,
        imageResId = R.drawable.dress2,
        description = "Lightweight floral print dress for warm days.",
        category = "Women"
    ),

    // Men
    Product(
        id = "m1",
        name = "Classic Blue Shirt",
        price = 39.99,
        imageResId = R.drawable.men_shirt1,
        description = "Stylish blue formal shirt made from premium cotton.",
        category = "Men"
    ),
    Product(
        id = "m2",
        name = "Denim Jacket",
        price = 69.99,
        imageResId = R.drawable.men_jacket1,
        description = "Trendy denim jacket perfect for casual outings.",
        category = "Men"
    ),

    // Accessories
    Product(
        id = "a1",
        name = "Stylish Handbag",
        price = 79.99,
        imageResId = R.drawable.bag1,
        description = "Classic leather handbag with gold finish.",
        category = "Accessories"
    ),
    Product(
        id = "a2",
        name = "Rose Gold Watch",
        price = 129.99,
        imageResId = R.drawable.watch1,
        description = "Elegant rose gold wristwatch with a sleek design.",
        category = "Accessories"
    ),

    // Optional extras for extended UI if needed
    Product(
        id = "a3",
        name = "High Heel Shoes",
        price = 89.99,
        imageResId = R.drawable.heels1,
        description = "Perfect pair of heels for fashion lovers.",
        category = "Accessories"
    )
)
