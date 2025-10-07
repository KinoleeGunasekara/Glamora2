package com.example.glamora.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * OrderDao provides database access methods for order-related operations.
 * Uses Room's transaction support to ensure atomic operations when creating orders.
 */
@Dao
interface OrderDao {

    /**
     * Inserts a new order and returns the generated orderId.
     * The return value (Long) is the auto-generated primary key,
     * which we need to link the order items to this order.
     */
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    /**
     * Inserts multiple order items in a single database operation.
     * More efficient than inserting items one by one.
     */
    @Insert
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    /**
     * Transaction method that creates a complete order with all its items atomically.
     * If any step fails, the entire operation is rolled back.
     * This prevents partial orders from being created.
     */
    @Transaction
    suspend fun insertCompleteOrder(order: OrderEntity, items: List<OrderItemEntity>) {
        // Step 1: Insert the order and get the generated ID
        val orderId = insertOrder(order)

        // Step 2: Update all items with the correct orderId and insert them
        val orderItemsWithId = items.map { it.copy(orderId = orderId.toInt()) }
        insertOrderItems(orderItemsWithId)
    }

    /**
     * Retrieves all orders sorted by date (newest first).
     * Returns Flow for reactive UI updates - the UI will automatically
     * refresh when new orders are added.
     */
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    /**
     * Retrieves all items for a specific order.
     * Used when user expands an order to see its contents.
     * This is a suspend function because it's called on-demand, not observed.
     */
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: Int): List<OrderItemEntity>
}
