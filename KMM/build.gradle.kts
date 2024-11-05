plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    kotlin("multiplatform").version(libs.versions.kotlin).apply(false)
    kotlin("kapt").version(libs.versions.kotlin).apply(false)
    id("com.android.application").version(libs.versions.agp).apply(false)
    id("com.android.library").version(libs.versions.agp).apply(false)
    id("org.jetbrains.compose").version(libs.versions.compose.plugin).apply(false)
}