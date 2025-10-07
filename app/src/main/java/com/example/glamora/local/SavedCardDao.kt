package com.example.glamora.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * SavedCardDao provides database access methods for managing saved payment cards.
 * Handles secure storage and retrieval of user's payment method preferences.
 * Uses Flow for reactive UI updates when card list changes.
 */
@Dao
interface SavedCardDao {

    /**
     * Inserts a new saved card into the database.
     * Called after successful payment when user opts to save card details.
     */
    @Insert
    suspend fun insertCard(card: SavedCardEntity)

    /**
     * Retrieves all saved cards ordered by default status and date added.
     * Default card appears first, then sorted by most recently added.
     * Returns Flow for reactive UI updates in PaymentMethodsScreen.
     */
    @Query("SELECT * FROM saved_cards ORDER BY isDefault DESC, dateAdded DESC")
    fun getAllSavedCards(): Flow<List<SavedCardEntity>>

    /**
     * Removes a specific saved card by its ID.
     * Called when user chooses to delete a payment method.
     */
    @Query("DELETE FROM saved_cards WHERE cardId = :cardId")
    suspend fun deleteCard(cardId: Int)

    /**
     * Clears default status from all cards.
     * Used before setting a new default card to ensure only one default exists.
     */
    @Query("UPDATE saved_cards SET isDefault = 0")
    suspend fun clearDefaultCards()

    /**
     * Sets a specific card as the default payment method.
     * Called after clearDefaultCards() to maintain single default constraint.
     */
    @Query("UPDATE saved_cards SET isDefault = 1 WHERE cardId = :cardId")
    suspend fun setDefaultCard(cardId: Int)

    /**
     * Checks if a card with the same last 4 digits already exists.
     * Prevents duplicate card entries for the same physical card.
     */
    @Query("SELECT COUNT(*) FROM saved_cards WHERE lastFourDigits = :lastFour AND cardType = :cardType")
    suspend fun cardExists(lastFour: String, cardType: String): Int
}
