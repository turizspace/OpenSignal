plugins {
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

val prop: (String) -> String? = { key -> project.findProperty(key) as String? }
val appName = prop("APP_NAME") ?: "OpenSignal"
val appVersionName = prop("APP_VERSION_NAME") ?: "1.0.0"

kotlin {
    jvm("desktop")
    jvmToolchain(17)

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
        }

        val desktopMain by getting {
            kotlin.srcDir("src/jvmMain/kotlin")
            resources.srcDir("src/jvmMain/resources")
            
            dependencies {
                implementation(project(":shared"))
                implementation(project(":charts"))
                implementation(project(":nostr"))
                implementation(project(":blossom"))
                implementation(project(":ai-inference"))
                implementation(compose.desktop.currentOs)
                implementation(compose.materialIconsExtended)
            }
        }
    }
}

dependencies {
}

compose.desktop {
    application {
        mainClass = "opensignal.MainKt"

        buildTypes {
            release {
                proguard {
                    isEnabled.set(false)
                }
            }
        }

        nativeDistributions {
            packageName = appName
            packageVersion = appVersionName
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
        }
    }
}
