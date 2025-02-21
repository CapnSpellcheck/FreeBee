//import io.github.ttypic.swiftklib.gradle.api.ExperimentalSwiftklibApi

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.ksp)
//    alias(libs.plugins.swiftklib)
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
        }
       iosTarget.compilations {
          val main by getting {
             cinterops {
                val FreeBeeData by creating {
                   definitionFile.set(project.file("src/nativeInterop/FreeBeeData/${iosTarget.name}/FreeBeeData.def"))
                   extraOpts("-libraryPath", "$projectDir/src/nativeInterop/FreeBeeData/${iosTarget.name}")
                   compilerOpts("-fmodules", "-I$projectDir/src/nativeInterop/FreeBeeData/${iosTarget.name}/FreeBeeData.build")
                }
                val KannaWrapper by creating {
                   definitionFile.set(project.file("src/nativeInterop/KannaWrapper/${iosTarget.name}/KannaWrapper.def"))
                   extraOpts("-libraryPath", "$projectDir/src/nativeInterop/KannaWrapper/${iosTarget.name}")
                   compilerOpts("-fmodules", "-I$projectDir/src/nativeInterop/KannaWrapper/${iosTarget.name}/KannaWrapper.build")
                }
//                create("FreeBeeData")
//                create("KannaWrapper")
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
            implementation(libs.kotlinx.coroutines.core)
            api(libs.multiplatform.logging)
            implementation(libs.kotlinx.datetime)
        }
       
       iosMain.dependencies {
          implementation(libs.ktor.client.darwin)
       }
       
       listOf(commonTest, androidUnitTest, iosTest).forEach { 
          it.dependencies {
             implementation(libs.kotlin.test)
             implementation(libs.kotlinx.coroutines.test)
             implementation(libs.multiplatform.settings.test)
             implementation(libs.ktor.client.mock)
          }
       }
       
       commonTest.dependencies {
          @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
          implementation(compose.uiTest)
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
        versionCode = 5
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
   add("kspAndroid", libs.androidx.room.compiler)
}

compose.resources {
   publicResClass = true
}

ksp {
   arg("room.generateKotlin", "true")
}
/**
 * NOTE: this is disabled because there is an issue with the unreleased version 0.7.0-SNAPSHOT
 * which need an unreleased feature (remote package dep)
 * I manually changed the code, built the plugin and used that to build
 * these libraries, then added the kotlin interop output to the repo
swiftklib {
    create("FreeBeeData") {
      path = file("src/iosMain/swift/data")
      packageName("com.letstwinkle.freebee.database.swift")
      minIos = "15.6"
      minMacos = ""
      minWatchos = ""
      minTvos = ""
      toolsVersion = "5.9"
   }
   create("KannaWrapper") {
      path = file("src/iosMain/swift/parsing")
      packageName("com.letstwinkle.freebee.screens.loader")
      minIos = "15.6"
      minMacos = ""
      minWatchos = ""
      minTvos = ""
      toolsVersion = "5.9"
      @OptIn(ExperimentalSwiftklibApi::class)
      dependencies {
         remote("Kanna") {
            url("https://github.com/tid-kijyun/Kanna")
            exactVersion("5.3.0")
         }
      }
   }
}
 **/
