import com.android.build.gradle.internal.ide.kmp.KotlinAndroidSourceSetMarker.Companion.android
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kapt)
//    alias(libs.plugins.compose.compiler)
}

kotlin {
    tasks.register("testClasses")
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            binaryOption("bundleId", "FreeBeeKMM")
            isStatic = true
            export("dev.icerock.moko:mvvm-core:${libs.versions.moko.mvvm.get()}")
        }
    }
    
    sourceSets {
        androidMain.dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.room.ktx)
        }

        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            // apparently not available in this version
//            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.datetime)
            
            api("dev.icerock.moko:mvvm-compose:${libs.versions.moko.mvvm.get()}") // api mvvm-core, getViewModel for Compose Multiplatform
            api("dev.icerock.moko:mvvm-flow-compose:${libs.versions.moko.mvvm.get()}") // api mvvm-flow, binding extensions for Compose Multiplatform
        }
        
        commonTest.dependencies {
            implementation("dev.icerock.moko:mvvm-test:${libs.versions.moko.mvvm.get()}") // test utilities
        }
    }
}

android {
    namespace = "com.letstwinkle.freebee"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.letstwinkle.freebee"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    "kapt"(libs.androidx.room.compiler)
}