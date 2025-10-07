package com.example.glamora.repository


import com.example.glamora.data.Product
import com.example.glamora.local.CartDao // Should now resolve correctly
import com.example.glamora.local.CartItemEntity // Should now resolve correctly
import com.example.glamora.local.OrderDao // Added import for OrderDao
import com.example.glamora.local.OrderEntity // Added import for OrderEntity
import com.example.glamora.local.OrderItemEntity // Added import for OrderItemEntity
import com.example.glamora.local.SavedCardDao // Added import for SavedCardDao
import com.example.glamora.local.SavedCardEntity // Added import for SavedCardEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID


/**
 * CartRepository now handles cart operations, order management, and saved payment cards.
 * Extended to include SavedCardDao for complete e-commerce payment functionality.
 * This unified approach reduces complexity by keeping related functionality together.
 */
class CartRepository(
    private val cartDao: CartDao,
    private val orderDao: OrderDao, // Added OrderDao to handle order operations
    private val savedCardDao: SavedCardDao // Added SavedCardDao to handle payment card storage
) {

    // REQUIREMENT: Read data from local source
    // Exposes the flow of cart items directly from the DAO
    fun getCartItems(): Flow<List<CartItemEntity>> {
        return cartDao.getAllCartItems() // This should return Flow, not suspend
    }

    // REQUIREMENT: Write data to local source (Add to Cart)
    suspend fun addItemToCart(product: Product) {
        val existingItem = cartDao.getCartItemById(product.id)

        if (existingItem != null) {
            // If item exists, increase quantity
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + 1)
            cartDao.updateCartItem(updatedItem)
        } else {
            // If item is new, insert it
            val newItem = CartItemEntity(
                productId = product.id,
                title = product.title,
                price = product.price,
                image = product.image,
                quantity = 1
            )
            cartDao.insertCartItem(newItem)
        }
    }

    // Used in CartScreen to remove an item
    suspend fun removeItemFromCart(item: CartItemEntity) {
        cartDao.deleteCartItem(item)
    }

    // Used in CartScreen to decrease quantity
    suspend fun decreaseItemQuantity(item: CartItemEntity) {
        if (item.quantity > 1) {
            val updatedItem = item.copy(quantity = item.quantity - 1)
            cartDao.updateCartItem(updatedItem)
        } else {
            // If quantity is 1, remove the item completely
            cartDao.deleteCartItem(item)
        }
    }

    /**
     * Deletes all items from the cart database.
     * This function is called by CartViewModel after a successful payment to empty the cart.
     */
    suspend fun clearCart() {
        // Calls the DAO method responsible for deleting all records from the cart table.
        cartDao.clearAllItems()
    }

    /**
     * Converts cart items to a completed order and saves it to database.
     * Called after successful payment processing in CartViewModel.
     * Generates unique order number and preserves product data at time of purchase.
     *
     * @param cartItems List of items currently in the cart
     * @param totalAmount Final calculated total (including taxes, shipping, etc.)
     * @return Generated order number for user confirmation
     */
    suspend fun saveOrder(cartItems: List<CartItemEntity>, totalAmount: Double): String {
        // Generate unique, human-readable order number using UUID
        val orderNumber = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"

        // Create the main order record with current timestamp
        val order = OrderEntity(
            orderNumber = orderNumber,
            totalAmount = totalAmount,
            orderDate = Date() // Current timestamp when order is completed
        )

        // Convert cart items to order items (preserving data at time of purchase)
        // This ensures order history remains accurate even if products change later
        val orderItems = cartItems.map { cartItem ->
            OrderItemEntity(
                orderId = 0, // Will be set by DAO after order insertion
                productId = cartItem.productId,
                title = cartItem.title,
                price = cartItem.price,
                quantity = cartItem.quantity,
                image = cartItem.image
            )
        }

        // Save complete order atomically using transaction
        orderDao.insertCompleteOrder(order, orderItems)
        return orderNumber
    }

    /**
     * Retrieves all orders for display in Order History screen.
     * Returns Flow for reactive UI updates - screen will automatically refresh when new orders are added.
     */
    fun getAllOrders(): Flow<List<OrderEntity>> = orderDao.getAllOrders()

    /**
     * Retrieves order items for a specific order.
     * Called when user expands an order card to see individual products.
     * Uses suspend function since it's called on-demand, not continuously observed.
     */
    suspend fun getOrderItems(orderId: Int): List<OrderItemEntity> = orderDao.getOrderItems(orderId)

    /**
     * Saves card details after successful payment when user opts to save card.
     * Only stores non-sensitive information for security compliance.
     * Prevents duplicate cards and manages default card selection.
     */
    suspend fun saveCardDetails(
        cardHolderName: String,
        cardNumber: String,
        expiryDate: String,
        cardType: String,
        setAsDefault: Boolean = false
    ) {
        // Extract only last 4 digits for security - full card number is never stored
        val lastFour = cardNumber.replace(" ", "").takeLast(4)

        // Check if this card already exists to prevent duplicates
        val cardExists = savedCardDao.cardExists(lastFour, cardType) > 0
        if (cardExists) {
            return // Don't save duplicate cards
        }

        // If this will be the default card, clear existing defaults first
        if (setAsDefault) {
            savedCardDao.clearDefaultCards()
        }

        val savedCard = SavedCardEntity(
            cardHolderName = cardHolderName,
            lastFourDigits = lastFour,
            expiryDate = expiryDate,
            cardType = cardType,
            isDefault = setAsDefault
        )

        savedCardDao.insertCard(savedCard)
    }

    /**
     * Retrieves all saved payment methods for display in PaymentMethodsScreen.
     * Returns Flow for reactive UI updates when cards are added/removed.
     */
    fun getAllSavedCards(): Flow<List<SavedCardEntity>> = savedCardDao.getAllSavedCards()

    /**
     * Removes a saved card by its unique ID.
     * Called when user deletes a payment method from PaymentMethodsScreen.
     */
    suspend fun deleteCard(cardId: Int) = savedCardDao.deleteCard(cardId)

    /**
     * Sets a specific card as the user's default payment method.
     * Automatically clears previous default to maintain single default constraint.
     */
    suspend fun setDefaultCard(cardId: Int) {
        savedCardDao.clearDefaultCards() // Clear existing defaults first
        savedCardDao.setDefaultCard(cardId) // Set new default
    }
}
