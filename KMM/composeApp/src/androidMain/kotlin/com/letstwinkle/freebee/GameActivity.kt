package com.letstwinkle.freebee

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.letstwinkle.freebee.screens.game.GameScreen

class GameActivity : ComponentActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      openIntent(intent)
   }
   
   override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
      openIntent(intent)
   }
   
   private fun openIntent(intent: Intent) {
      if (intent.hasGame()) {
         setContent {
            GameScreen(repository(), intent.getGameExtra()!!, this.backNavigator())
         }
      } else if (intent.hasGameDate() && intent.hasGameIdentifier()) {
         setContent {
            GameScreen(repository(), intent.getGameIdentifierExtra(), intent.getGameDateExtra(), this.backNavigator())
         }
      } else {
         finish()
      }
   }
}
