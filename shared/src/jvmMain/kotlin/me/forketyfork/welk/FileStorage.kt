package me.forketyfork.welk

import java.io.File

/**
 * Simple file-based key-value storage used for persisting Firebase auth data.
 * Each key is stored as a separate file inside the given directory.
 */
class FileStorage(
    private val directory: File,
) {
    init {
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    fun store(
        key: String,
        value: String,
    ) {
        File(directory, key).writeText(value)
    }

    fun retrieve(key: String): String? {
        val file = File(directory, key)
        return if (file.exists()) file.readText() else null
    }

    fun clear(key: String) {
        File(directory, key).delete()
    }
}
