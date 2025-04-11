enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "findroid"

include(":app:phone")
include(":app:tv")
include(":core")
include(":data")
include(":player:core")
include(":player:video")
include(":setup")
include(":modes:film")
include(":settings")

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