enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "findroid"

include(":app:phone")
include(":app:tv")
include(":core")
include(":data")
include(":preferences")
include(":player:core")
include(":player:video")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()

        maven("https://jitpack.io") {
            content {
                includeVersionByRegex("com.github.fengymi.*", ".*", ".*")
            }
        }
    }
}