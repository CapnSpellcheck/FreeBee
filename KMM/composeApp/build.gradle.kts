plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kapt)
    alias(libs.plugins.swiftklib)
    alias(libs.plugins.compose.compiler)
    id("kotlin-parcelize")
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
       iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            binaryOption("bundleId", "FreeBeeKMM")
            isStatic = true
            export(libs.kotlinx.datetime)
        }
       iosTarget.compilations {
          val main by getting {
             cinterops {
                create("FreeBeeData")
             }
          }
       }
    }
    
    sourceSets {
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.room.ktx)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.work)
            implementation("org.jsoup:jsoup:1.18.3")
        }

        @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.multiplatform.settings)
            implementation(libs.ktor.client.core)
            implementation(libs.kamel.image)
            api(libs.moko.mvvm.flow)
            api(libs.multiplatform.logging)
            api(libs.kotlinx.datetime) // api for exporting
        }
       
       iosMain.dependencies {
          implementation(libs.ktor.client.darwin)
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    "kapt"(libs.androidx.room.compiler)
}

compose.resources {
   publicResClass = true
}

swiftklib {
   create("FreeBeeData") {
      path = file("src/iosMain/swift")
      packageName("com.letstwinkle.freebee.database.swift")
   }
}
