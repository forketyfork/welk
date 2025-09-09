package me.forketyfork.welk.domain

import kotlinx.serialization.Serializable

/**
 * Enum representing the user's assessment of how well they knew a card during review.
 */
@Serializable
enum class ReviewGrade {
    AGAIN, // Card was completely forgotten - review again soon
    HARD, // Card was difficult to remember - review sooner than normal
    GOOD, // Card was remembered correctly - review at normal interval
    EASY, // Card was very easy to remember - review after longer interval
}
