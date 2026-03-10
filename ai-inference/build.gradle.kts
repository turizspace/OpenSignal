plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

kotlin {
    androidTarget()
    jvm("desktop")
    jvmToolchain(17)

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(libs.onnxruntime)
            implementation(libs.tess4j)
        }

        val androidMain by getting
        androidMain.dependencies {
            implementation(libs.onnxruntime.android)
            implementation(libs.mlkit.text.recognition)
        }
    }
}

android {
    namespace = "opensignal.ai"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
