package com.letstwinkle.freebee

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.letstwinkle.freebee.screens.loader.GameLoaderScreen
import kotlinx.datetime.LocalDate

class GameLoaderActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      
      val gameDate = intent.getGameDateExtra()
      setContent { GameLoaderScreen(gameDate, this.backNavigator()) }
   }
}

fun Intent.putGameDateExtra(date: LocalDate) {
   putExtra("gamedate", date.toEpochDays())
}

fun Intent.getGameDateExtra(): LocalDate = LocalDate.fromEpochDays(getIntExtra("gamedate", 0))
