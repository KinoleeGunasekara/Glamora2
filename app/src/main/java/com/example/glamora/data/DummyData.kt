package com.example.glamora.data

import com.example.glamora.R

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageResId: Int,
    val description: String
)

val sampleProducts = listOf(
    Product(
        id = "p1",
        name = "Elegant Pink Dress",
        price = 59.99,
        imageResId = R.drawable.dress1,
        description = "A beautiful flowing pink dress for all occasions."
    ),
    Product(
        id = "p2",
        name = "Stylish Handbag",
        price = 79.99,
        imageResId = R.drawable.bag1,
        description = "Classic leather handbag with gold finish."
    ),
    Product(
        id = "p3",
        name = "High Heel Shoes",
        price = 89.99,
        imageResId = R.drawable.heels1,
        description = "Perfect pair of heels for fashion lovers."
    ),
    Product(
        id = "p4",
        name = "Floral Summer Dress",
        price = 49.99,
        imageResId = R.drawable.dress2,
        description = "Lightweight floral print dress for warm days."
    )
)
