package me.forketyfork.welk.domain

import kotlinx.serialization.Serializable

/**
 * Data class representing a single flashcard with front and back.
 */
@Serializable
data class Card(
    val front: String = "",
    val back: String = "",
    var learned: Boolean = false
)