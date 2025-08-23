package me.forketyfork.welk.domain

import kotlin.time.Instant
import kotlinx.serialization.Serializable

/**
 * Record of a single review session for a flashcard.
 * Contains the timestamp when the review was done and the grade assigned by the user.
 */
@Serializable
data class CardReview(
    val timestamp: Instant,
    val grade: ReviewGrade
)