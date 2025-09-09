import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

dependencies {
    detektPlugins(libs.detekt.compose.rules)
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    dependencies {
        detektPlugins(rootProject.libs.detekt.compose.rules)
    }

    detekt {
        buildUponDefaultConfig = true
        allRules = false
        config.from("$rootDir/detekt.yaml")
        baseline = file("$projectDir/detekt-baseline.xml")
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        setSource(files("src"))
        include("**/*.kt")
        exclude("**/build/**", "**/generated/**")

        reports {
            html.required.set(true)
            xml.required.set(true)
            txt.required.set(true)
            sarif.required.set(true)
            md.required.set(true)
        }
    }

    tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
        setSource(files("src"))
        include("**/*.kt")
        exclude("**/build/**", "**/generated/**")
    }

    ktlint {
        version.set(
            rootProject.libs.versions.ktlint.version
                .get(),
        )
        ignoreFailures.set(false)
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.CHECKSTYLE)
        }
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }
}
