package me.forketyfork.welk

import android.app.Application
import co.touchlab.kermit.Logger
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.initialize
import java.io.File
import java.util.Properties
import me.forketyfork.welk.FileStorage

class JVMPlatform : Platform {

    override val name: String = "Java ${System.getProperty("java.version")}"

    companion object {
        private val logger = Logger.withTag("JVMPlatform")

        private val firestore = lazy {
            lazyInitializeFirestore()
        }

        private val fileStorage = FileStorage(File(System.getProperty("user.home"), ".welk/firebase"))

        private fun lazyInitializeFirestore(): FirebaseFirestore {

            FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
                override fun store(key: String, value: String) = fileStorage.store(key, value)
                override fun retrieve(key: String) = fileStorage.retrieve(key)
                override fun clear(key: String) = fileStorage.clear(key)

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
            val inputStream =
                this::class.java.classLoader.getResourceAsStream(firebasePropertiesFile)
            if (inputStream != null) {
                inputStream.use(properties::load)
            } else {
                error("Failed to load the $firebasePropertiesFile file")
            }
            return properties
        }

    }

    override fun initializeFirestore(): FirebaseFirestore {
        return firestore.value
    }

}

actual fun getPlatform(): Platform = JVMPlatform()
