package me.forketyfork.welk.domain

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Record of a single review session for a flashcard.
 * Contains the timestamp when the review was done and the grade assigned by the user.
 */
@Serializable
data class CardReview(
    val timestamp: Instant,
    val grade: ReviewGrade,
)
