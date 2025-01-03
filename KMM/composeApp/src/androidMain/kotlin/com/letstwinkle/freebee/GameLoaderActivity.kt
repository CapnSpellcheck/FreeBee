package com.letstwinkle.freebee

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.letstwinkle.freebee.database.EntityIdentifier
import com.letstwinkle.freebee.screens.loader.GameLoaderNavigator
import com.letstwinkle.freebee.screens.loader.GameLoaderScreen
import kotlinx.datetime.LocalDate

class GameLoaderActivity : ComponentActivity(), GameLoaderNavigator {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      
      val gameDate = intent.getGameDateExtra()
      setContent { GameLoaderScreen(gameDate, this.backNavigator(), this) }
   }
   
   override fun openGame(gameDate: LocalDate, gameID: EntityIdentifier) {
      val intent = Intent(this, GameActivity::class.java)
      intent.putGameDateExtra(gameDate)
      intent.putGameIdentifierExtra(gameID)
      intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
      startActivity(intent)
      finish()
   }
}
