package com.letstwinkle.freebee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.letstwinkle.freebee.screens.game.GameScreen

class GameActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      
      if (intent.hasGame()) {
         setContent {
            GameScreen(intent.getGameExtra()!!, this.backNavigator())
         }
      } else if (intent.hasGameDate() && intent.hasGameIdentifier()) {
         setContent {
            GameScreen(intent.getGameIdentifierExtra(), intent.getGameDateExtra(), this.backNavigator())
         }
      } else {
         finish()
      }
   }
}
