package com.example.glamora.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glamora.data.Product
import com.example.glamora.local.CartItemEntity
import com.example.glamora.local.OrderEntity // Added import for OrderEntity
import com.example.glamora.local.OrderItemEntity // Added import for OrderItemEntity
import com.example.glamora.local.SavedCardEntity // Added import for SavedCardEntity
import com.example.glamora.repository.CartRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow // Added import for MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Sealed class defining one-time events for the UI (e.g., confirmation messages or navigation prompts).
sealed class CartUiEvent {
    // Event emitted after a product is successfully added, carrying the product name.
    data class ItemAdded(val productName: String) : CartUiEvent()
}

/**
 * CartViewModel now handles both cart operations and order history management.
 * Extended to include order-related StateFlows and methods for complete e-commerce flow.
 * This unified approach reduces complexity by keeping related functionality together.
 */
class CartViewModel(private val repository: CartRepository) : ViewModel() {

    // SharedFlow used to emit non-state, one-time events to the UI (e.g., triggering a Snackbar).
    private val _eventFlow = MutableSharedFlow<CartUiEvent>()
    val eventFlow: SharedFlow<CartUiEvent> = _eventFlow

    // StateFlow holding the live, reactive list of cart items fetched from the local database.
    // The UI (CartScreen) observes and displays this list automatically upon changes.
    val cartItems: StateFlow<List<CartItemEntity>> = repository.getCartItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keeps the flow active while the UI is observing.
            initialValue = emptyList()
        )

    // StateFlow that calculates and holds the total cost of all items in the cart.
    // It automatically recalculates whenever the 'cartItems' list changes.
    val cartTotal: StateFlow<Double> = cartItems.map { items ->
        // Calculates the sum of (price * quantity) for every item in the list.
        items.sumOf { it.price * it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    /**
     * StateFlow containing all user orders for Order History screen.
     * Automatically updates when new orders are created, providing reactive UI updates.
     * Orders are sorted by date (newest first) in the repository query.
     */
    val orders: StateFlow<List<OrderEntity>> = repository.getAllOrders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep active for 5 seconds after last subscriber
            initialValue = emptyList()
        )

    /**
     * StateFlow storing order items for each expanded order.
     * Key: orderId, Value: List of items for that order
     * Items are loaded on-demand when user expands an order card.
     */
    private val _orderItems = MutableStateFlow<Map<Int, List<OrderItemEntity>>>(emptyMap())
    val orderItems: StateFlow<Map<Int, List<OrderItemEntity>>> = _orderItems

    /**
     * StateFlow containing all user's saved payment cards for PaymentMethodsScreen.
     * Automatically updates when cards are added, deleted, or default status changes.
     * Cards are sorted by default status first, then by date added (newest first).
     */
    val savedCards: StateFlow<List<SavedCardEntity>> = repository.getAllSavedCards()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep active for 5 seconds after last subscriber
            initialValue = emptyList()
        )

    // Adds a product to the cart by either inserting a new item or incrementing the quantity of an existing one.
    // This is called by ProductDetailScreen, HomeScreen, and DiscoverScreen.
    fun addItem(product: Product) {
        viewModelScope.launch {
            // Delegates persistence logic (insert or update) to the repository.
            repository.addItemToCart(product)

            // Emits a success event for the UI to show a confirmation message (like a Snackbar).
            _eventFlow.emit(CartUiEvent.ItemAdded(product.title))
        }
    }

    // Removes a specific item entity entirely from the cart database.
    fun removeItem(item: CartItemEntity) {
        viewModelScope.launch {
            repository.removeItemFromCart(item)
        }
    }

    // Adjusts the quantity of an existing cart item by incrementing or decrementing.
    fun updateQuantity(item: CartItemEntity, change: Int) {
        viewModelScope.launch {
            if (change == 1) {
                // If increasing quantity, converts the entity to a Product and uses the addItem logic.
                repository.addItemToCart(item.toProduct())
            } else if (change == -1) {
                // If decreasing quantity, calls logic to decrement the count or remove the item if quantity reaches zero.
                repository.decreaseItemQuantity(item)
            }
        }
    }

    /**
     * Clears the entire cart after a successful transaction.
     * This function is called from PaymentScreen.kt.
     */
    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    /**
     * Completes the checkout process by converting cart to order.
     * Called after successful payment processing in PaymentScreen.
     * Automatically clears the cart after order creation to prevent duplicate orders.
     *
     * @return Order number if successful, null if failed or cart is empty
     */
    suspend fun completeOrder(): String? {
        return try {
            val currentCartItems = cartItems.value
            val currentTotal = cartTotal.value

            if (currentCartItems.isNotEmpty()) {
                // Save order to database with current cart contents and total
                val orderNumber = repository.saveOrder(currentCartItems, currentTotal)

                // Clear cart after successful order creation
                clearCart()

                orderNumber
            } else {
                null // Return null if cart is empty
            }
        } catch (e: Exception) {
            // In production, log this exception for debugging
            null // Return null on any error during order creation
        }
    }

    /**
     * Loads order items for a specific order when user expands order card.
     * Results are cached in _orderItems StateFlow to avoid repeated database calls.
     * Called from OrderHistoryScreen when user wants to see order details.
     */
    fun loadOrderItems(orderId: Int) {
        viewModelScope.launch {
            try {
                val items = repository.getOrderItems(orderId)
                // Update the StateFlow with new order items, preserving existing data
                _orderItems.value = _orderItems.value.toMutableMap().apply {
                    put(orderId, items)
                }
            } catch (e: Exception) {
                // In production, handle this error appropriately
                // For now, we'll leave the items list empty if loading fails
            }
        }
    }

    /**
     * Saves card details when user completes payment with "Save Card" checkbox checked.
     * Only saves if user explicitly opted to save card during checkout process.
     * Automatically sets first saved card as default for user convenience.
     */
    fun saveCardDetails(
        cardHolderName: String,
        cardNumber: String,
        expiryDate: String,
        cardType: String,
        saveCard: Boolean
    ) {
        if (saveCard) {
            viewModelScope.launch {
                try {
                    repository.saveCardDetails(
                        cardHolderName = cardHolderName,
                        cardNumber = cardNumber,
                        expiryDate = expiryDate,
                        cardType = cardType,
                        setAsDefault = savedCards.value.isEmpty() // First card becomes default automatically
                    )
                } catch (e: Exception) {
                    // In production, log this exception for debugging
                    // For now, silently fail to avoid disrupting checkout flow
                }
            }
        }
    }

    /**
     * Deletes a saved payment method by its unique ID.
     * Called from PaymentMethodsScreen when user confirms card deletion.
     * UI automatically updates via StateFlow when deletion completes.
     */
    fun deleteCard(cardId: Int) {
        viewModelScope.launch {
            try {
                repository.deleteCard(cardId)
            } catch (e: Exception) {
                // In production, show error message to user
                // For now, silently fail
            }
        }
    }

    /**
     * Sets a specific card as the user's default payment method.
     * Called from PaymentMethodsScreen when user selects "Set Default".
     * Automatically clears previous default to maintain single default constraint.
     */
    fun setDefaultCard(cardId: Int) {
        viewModelScope.launch {
            try {
                repository.setDefaultCard(cardId)
            } catch (e: Exception) {
                // In production, show error message to user
                // For now, silently fail
            }
        }
    }
}

// Extension function to convert a database CartItemEntity back into a generic Product model.
fun CartItemEntity.toProduct() = Product(
    id = this.productId,
    title = this.title,
    price = this.price,
    description = "", // These fields are intentionally left blank as they are not stored in the entity.
    category = "",
    image = this.image
)
