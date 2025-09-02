rootProject.name = "FreeBee"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/CapnSpellcheck/cmp-animatedcounter")
            credentials {
                username = System.getenv("GITHUB_USER") ?: settings.extra.properties["GITHUB_USER"] as String
                password = System.getenv("GITHUB_PERSONAL_ACCESS_TOKEN") ?: settings.extra.properties["GITHUB_PERSONAL_ACCESS_TOKEN"] as String
            }
        }
    }
}

include(":composeApp")
