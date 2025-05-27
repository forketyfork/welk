package me.forketyfork.welk.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Data class representing a single flashcard with front and back.
 */
@Serializable
data class Card(
    // Document ID in Firestore
    @Transient
    var id: String? = null,
    // Reference to parent deck
    val deckId: String,
    val front: String,
    val back: String,
    var learned: Boolean = false,
    // Position within the deck for ordering
    val position: Int = 0
)