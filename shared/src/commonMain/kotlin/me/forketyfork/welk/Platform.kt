package me.forketyfork.welk

import dev.gitlive.firebase.firestore.FirebaseFirestore

interface Platform {
    val name: String

    fun initializeFirestore(): FirebaseFirestore
}

expect fun getPlatform(): Platform
