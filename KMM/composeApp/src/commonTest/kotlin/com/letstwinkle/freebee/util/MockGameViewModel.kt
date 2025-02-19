package com.letstwinkle.freebee.util

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.letstwinkle.freebee.database.*
import com.letstwinkle.freebee.screens.game.IGameViewModel
import kotlinx.coroutines.channels.Channel

class MockGameViewModel(private val game: MockGame) : IGameViewModel<MockGame> {
   private val gameWithWordsMutable = mutableStateOf(game, policy = neverEqualPolicy())
   
   override val gameProgress: Float
      get() = 0.5f
   override val gameWithWords: State<MockGame?>
      get() = gameWithWordsMutable
   override val entryNotAcceptedEvents = Channel<String>()
   override val enteredWordSummary: String
      get() = "No words yet"
   override val enteredWordSummaryColor: Color
      get() = Color.Black
   override val currentWordDisplay: String
      get() = game.currentWordDisplay
      
   override fun backspace() {
   }
   
   override fun append(letter: Char) {
      game.currentWord += letter
      gameWithWordsMutable.value = gameWithWordsMutable.value
   }
   
   override suspend fun enter() {
   }
   
   override val enterEnabled: Boolean
      get() = game.currentWord.length >= 4
}
