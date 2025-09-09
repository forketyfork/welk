package me.forketyfork.welk.domain.sra

import me.forketyfork.welk.domain.CardReview
import me.forketyfork.welk.domain.ReviewGrade
import kotlin.time.Instant

/**
 * Interface for implementing spaced repetition algorithms.
 * Algorithms calculate when a card should next be reviewed based on review history and current grade.
 */
interface SpacedRepetitionAlgorithm {
    /**
     * Calculate the next review timestamp based on review history and current grade.
     *
     * @param reviews Complete review history for the card, ordered by timestamp ascending
     * @param currentGrade The grade just assigned by the user
     * @param currentTime Current timestamp
     * @return Timestamp for next review
     */
    fun calculateNextReview(
        reviews: List<CardReview>,
        currentGrade: ReviewGrade,
        currentTime: Instant,
    ): Instant
}
