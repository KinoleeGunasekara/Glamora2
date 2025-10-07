package com.example.glamora.local
import com.example.glamora.local.CartItemEntity
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Assuming CartItemEntity is also defined in this package: com.example.glamora.data.local

@Dao
interface CartDao {

    // REQUIREMENT: Read data from a local data source (CartScreen)
    // Returns Flow so the UI can observe changes reactively
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    // REQUIREMENT: Write data to a local data source (ProductDetailScreen)
    // If the item already exists (based on PrimaryKey), replace it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    // Function to update item quantity (optional, but good for a cart)
    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    // Used for removing an item from the cart
    @Delete
    suspend fun deleteCartItem(item: CartItemEntity)

    // Helper to get an item by ID (useful for checking if it exists before insert/update)
    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun getCartItemById(productId: Int): CartItemEntity?

    /**
     * Deletes all items from the cart table.
     * This supports the payment flow by clearing the cart after a successful transaction.
     */
    @Query("DELETE FROM cart_items")
    suspend fun clearAllItems()
}
