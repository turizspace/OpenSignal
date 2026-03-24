import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

val prop: (String) -> String? = { key -> project.findProperty(key) as String? }
val appName = prop("APP_NAME") ?: "OpenSignal"
val appVersionName = prop("APP_VERSION_NAME") ?: "1.0.0"
val appVersionCode = prop("APP_VERSION_CODE")?.toIntOrNull() ?: 1
val keystoreProps = Properties().apply {
    val keystoreFile = rootProject.file("keystore.properties")
    if (keystoreFile.exists()) {
        keystoreFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "opensignal.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "opensignal.android"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
    }

    val ksPath = System.getenv("CI_ANDROID_KEYSTORE_PATH")
        ?: prop("CI_ANDROID_KEYSTORE_PATH")
        ?: prop("RELEASE_STORE_FILE")
        ?: keystoreProps.getProperty("storeFile")?.let { rootProject.file(it).path }
    val ksStorePassword = System.getenv("CI_ANDROID_KEYSTORE_PASSWORD")
        ?: prop("RELEASE_STORE_PASSWORD")
        ?: keystoreProps.getProperty("storePassword")
    val ksKeyAlias = System.getenv("CI_ANDROID_KEY_ALIAS")
        ?: prop("RELEASE_KEY_ALIAS")
        ?: keystoreProps.getProperty("keyAlias")
    val ksKeyPassword = System.getenv("CI_ANDROID_KEY_PASSWORD")
        ?: prop("RELEASE_KEY_PASSWORD")
        ?: keystoreProps.getProperty("keyPassword")
    val hasReleaseSigning = ksPath != null && ksStorePassword != null && ksKeyAlias != null && ksKeyPassword != null
    val needsRelease = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
    val isCi = (System.getenv("CI") ?: prop("CI"))?.toBoolean() == true

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = file(ksPath!!)
                storePassword = ksStorePassword
                keyAlias = ksKeyAlias
                keyPassword = ksKeyPassword
            } else {
                if (needsRelease && isCi) {
                    throw GradleException(
                        "Release signing config missing. Provide CI_ANDROID_KEYSTORE_PATH, " +
                            "CI_ANDROID_KEYSTORE_PASSWORD, CI_ANDROID_KEY_ALIAS, CI_ANDROID_KEY_PASSWORD " +
                            "or set the corresponding RELEASE_* properties in gradle.properties."
                    )
                }
                if (needsRelease) {
                    logger.warn(
                        "Release signing config missing. Falling back to debug signing for this release build."
                    )
                } else {
                    logger.warn(
                        "Release signing config not set. Release builds will use debug signing until credentials are provided."
                    )
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":charts"))
    implementation(project(":nostr"))
    implementation(project(":blossom"))
    implementation(project(":ai-inference"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.security.crypto)

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
