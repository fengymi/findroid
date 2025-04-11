plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "dev.jdtech.jellyfin.player.video"
    compileSdk = Versions.compileSdk
    buildToolsVersion = Versions.buildTools

    defaultConfig {
        minSdk = Versions.minSdk
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
        }
        register("staging") {
            initWith(getByName("release"))
        }
    }

    compileOptions {
        sourceCompatibility = Versions.java
        targetCompatibility = Versions.java
    }
}

ktlint {
    version.set(Versions.ktlint)
    android.set(true)
    ignoreFailures.set(false)
}

dependencies {
    implementation(projects.player.core)
    implementation(projects.data)
    implementation(projects.settings)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.jellyfin.core)
    implementation(libs.libmpv)
    implementation(libs.material)
    implementation(libs.timber)

    // bilibili 弹幕
    implementation(libs.danmaku.render.engine)
    implementation(libs.danmaku.render.engine.ndk.armv7a)
    implementation(libs.danmaku.render.engine.ndk.x86)
    implementation(libs.danmaku.render.engine.ndk.armv5)

    implementation(libs.alibaba.json)
}
