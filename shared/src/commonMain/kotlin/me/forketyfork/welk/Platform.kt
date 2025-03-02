package me.forketyfork.welk

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
