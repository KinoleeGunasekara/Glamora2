package com.example.glamora.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * OrderEntity represents the main order record in the database.
 * This table stores the header information for each completed order.
 * Each order can contain multiple products (stored in OrderItemEntity).
 */
@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val orderId: Int = 0, // Auto-generated primary key for internal database operations

    val orderNumber: String, // Human-readable order number shown to users (e.g., "ORD-AB12CD34")
    val totalAmount: Double, // Final total price after all calculations
    val orderDate: Date, // Timestamp when the order was placed (requires TypeConverters)
    val status: String = "Completed" // Order status - currently defaults to "Completed" but can be expanded
)
