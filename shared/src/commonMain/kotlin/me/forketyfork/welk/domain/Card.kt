package me.forketyfork.welk.domain

import kotlin.time.Instant
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
    // Ordered list of review records, sorted by timestamp ascending
    val reviews: List<CardReview> = emptyList(),
    // Nullable timestamp indicating when the card should next be reviewed
    val nextReview: Instant? = null,
    // Position within the deck for ordering
    val position: Int = 0
)