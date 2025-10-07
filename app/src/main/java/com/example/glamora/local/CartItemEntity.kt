package com.example.glamora.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: Int, // Unique identifier
    val title: String,
    val image: String,
    val price: Double,
    val quantity: Int
)
