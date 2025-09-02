This is a Kotlin Multiplatform project targeting Android, iOS.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


**NEW Sep 2 2025!** This project uses a Github package server, which requires you to set up a **classic** PAT. (See the information on GitHub Docs)[https://docs.github.com/en/packages/learn-github-packages/introduction-to-github-packages#authenticating-to-github-packages]. See ```settings.gradle.kts``` in this folder for the names of the environment variables or Gradle properties you must set with the credentials.