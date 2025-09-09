package me.forketyfork.welk.domain.sra

import me.forketyfork.welk.domain.CardReview
import me.forketyfork.welk.domain.ReviewGrade
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Simple spaced repetition algorithm with fixed intervals based on grade.
 * This is a basic implementation that uses predetermined intervals without considering
 * previous review history or difficulty factors.
 */
class SimpleIntervalAlgorithm : SpacedRepetitionAlgorithm {
    override fun calculateNextReview(
        reviews: List<CardReview>,
        currentGrade: ReviewGrade,
        currentTime: Instant,
    ): Instant =
        when (currentGrade) {
            ReviewGrade.AGAIN -> currentTime.plus(10.minutes)
            ReviewGrade.HARD -> currentTime.plus(1.hours)
            ReviewGrade.GOOD -> currentTime.plus(1.days)
            ReviewGrade.EASY -> currentTime.plus(7.days)
        }
}
