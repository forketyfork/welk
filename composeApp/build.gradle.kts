import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.FileInputStream
import java.util.*

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidApplication)
}

kotlin {
    jvm("desktop")

    // currently there's no Android application, this is only needed for `@Preview` annotations to work
    androidTarget {
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(projects.shared)
            implementation(libs.compose.material.ripple)
            implementation(libs.kermit)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))

            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.koin.test)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "me.forketyfork.welk.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "me.forketyfork.welk"
            packageVersion = "1.0.0"
            macOS {
                iconFile.set(project.file("welk_icon.icns"))
                jvmArgs(
                    "-Dapple.awt.application.appearance=system"
                )
            }
        }
    }
}

// load test user and password from local.properties
val localProperties = Properties()
localProperties.load(FileInputStream(rootProject.file("local.properties")))
tasks.withType<Test>() {
    listOf("WELK_TEST_USERNAME", "WELK_TEST_PASSWORD").forEach { key ->
        systemProperty(key, localProperties.getProperty(key))
    }
}

android {
    namespace = "com.forketyfork.welk"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
}