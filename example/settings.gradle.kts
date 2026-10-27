pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AbrovaAuthExample"

include(":app")

// Abrova Auth SDK (local module)
include(":abrova-auth")
project(":abrova-auth").projectDir = file("../abrova-auth")
