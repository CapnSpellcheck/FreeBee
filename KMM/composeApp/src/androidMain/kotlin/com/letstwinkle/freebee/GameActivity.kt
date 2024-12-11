package com.letstwinkle.freebee

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.IntentCompat
import com.letstwinkle.freebee.database.Game
import com.letstwinkle.freebee.screens.game.GameScreen

class GameActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      
      intent.getGameExtra()?.let { game ->
         setContent {
            GameScreen(game, this.backNavigator())
         }
      }
   }
}

fun Intent.putGameExtra(game: Game) {
   putExtra("game", game)
}

fun Intent.getGameExtra(): Game? = IntentCompat.getParcelableExtra(this, "game", Game::class.java)
