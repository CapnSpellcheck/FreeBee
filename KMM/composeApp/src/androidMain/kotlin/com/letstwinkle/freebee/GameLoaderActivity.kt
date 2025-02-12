package com.letstwinkle.freebee

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.letstwinkle.freebee.screens.loader.GameLoaderNavigator
import com.letstwinkle.freebee.screens.loader.GameLoaderScreen
import kotlinx.datetime.*

class GameLoaderActivity : ComponentActivity(), GameLoaderNavigator<Long> {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      val gameDate: LocalDate = if (intent.hasExtra("TODAY")) {
         Clock.System.todayIn(TimeZone.currentSystemDefault())
      } else {
         intent.getGameDateExtra()
      }
      setContent { 
         GameLoaderScreen(repository(), gameDate, this.backNavigator(), this)
      }
   }
   
   override fun openGame(gameDate: LocalDate, gameID: Long) {
      val intent = Intent(this, GameActivity::class.java)
      intent.putGameDateExtra(gameDate)
      intent.putGameIdentifierExtra(gameID)
      intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
      startActivity(intent)
      finish()
   }
}
