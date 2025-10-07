package com.example.glamora.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * OrderItemEntity stores individual products within an order.
 * This creates a one-to-many relationship: one Order has many OrderItems.
 * The foreign key constraint ensures data integrity - if an order is deleted,
 * all its items are automatically deleted (CASCADE).
 */
@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["orderId"], // References OrderEntity.orderId
            childColumns = ["orderId"],  // This entity's orderId field
            onDelete = ForeignKey.CASCADE // Delete all items if parent order is deleted
        )
    ]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Unique ID for this order item record

    val orderId: Int, // Foreign key linking to OrderEntity
    val productId: Int, // Original product ID from the product catalog
    val title: String, // Product name (stored to preserve data even if product is deleted)
    val price: Double, // Price at time of purchase (important for historical accuracy)
    val quantity: Int, // Number of units purchased
    val image: String // Product image URL (stored for display in order history)
)
