package com.letstwinkle.freebee

enum class UserActivity(private val key: String) {
   GameList("index"),
   Picker("picker"),
   Loader("loader"),
   Game("game"),
   ;
   val activityType: String
      get() = "com.letstwinkle.freebee.$key"
}

object UserActivityKeys {
   const val pickerDate = "pickerDate" // TODO: I never implemented this
   const val gameDate = "date"
   const val gameEnteredLetters = "enteredLetters"
   const val gameURL = "url"
}
