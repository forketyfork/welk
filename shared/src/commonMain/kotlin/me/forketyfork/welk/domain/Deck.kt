package me.forketyfork.welk.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Data class representing a deck of flashcards.
 * Following Firestore best practices, this uses a separate collection for decks.
 */
@Serializable
data class Deck(
    // Document ID in Firestore
    @Transient
    var id: String? = null,
    val name: String = "",
    val description: String = "",
    // We denormalize the card count for quick access without loading all cards
    val cardCount: Int = 0,
    // Useful for UI display and organization
    val lastModified: Long = 0,
    val created: Long = 0
)