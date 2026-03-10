plugins {
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":charts"))
    implementation(project(":nostr"))
    implementation(project(":blossom"))
    implementation(project(":ai-inference"))
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
}

compose.desktop {
    application {
        mainClass = "opensignal.MainKt"

        nativeDistributions {
            packageName = "OpenSignal"
            packageVersion = "1.0.0"
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
        }
    }
}
