package me.forketyfork.welk

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.app
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override fun initializeFirestore(): FirebaseFirestore {
        return Firebase.firestore(Firebase.app("iosApp"))
    }

}

actual fun getPlatform(): Platform = IOSPlatform()
