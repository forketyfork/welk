package me.forketyfork.welk

import android.app.Application
import co.touchlab.kermit.Logger
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import java.util.Properties

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"

    override fun initializeFirestore(): FirebaseFirestore {

        val logger = Logger.withTag("FirebaseFirestore")

        FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
            val storage = mutableMapOf<String, String>()
            override fun store(key: String, value: String) = storage.set(key, value)
            override fun retrieve(key: String) = storage[key]
            override fun clear(key: String) {
                storage.remove(key)
            }

            override fun log(msg: String) = logger.d(msg)
        })
        // Access the keys
        val firebaseProperties = loadApiKeys()

        return Firebase.firestore(
            Firebase.initialize(
                Application(),
                options = FirebaseOptions(
                    apiKey = firebaseProperties.getProperty("firebase.apiKey"),
                    authDomain = firebaseProperties.getProperty("firebase.authDomain"),
                    projectId = firebaseProperties.getProperty("firebase.projectId"),
                    storageBucket = firebaseProperties.getProperty("firebase.storageBucket"),
                    gcmSenderId = firebaseProperties.getProperty("firebase.messagingSenderId"),
                    applicationId = firebaseProperties.getProperty("firebase.appId")
                )
            )
        )
    }

    private fun loadApiKeys(): Properties {
        val firebasePropertiesFile = "firebase.properties"
        val properties = Properties()
        val inputStream = this.javaClass.classLoader.getResourceAsStream(firebasePropertiesFile)
        if (inputStream != null) {
            inputStream.use(properties::load)
        } else {
            error("Failed to load the $firebasePropertiesFile file")
        }
        return properties
    }

}

actual fun getPlatform(): Platform = JVMPlatform()
