package com.letstwinkle.freebee.screens.game

import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import com.letstwinkle.freebee.database.IGameWithWords
import kotlinx.coroutines.channels.Channel

interface IGameViewModel<GameWithWords: IGameWithWords<*>> {
   val gameProgress: Float
   val gameWithWords: State<GameWithWords?>
   val entryNotAcceptedEvents: Channel<String>
   val enteredWordSummary: String
   val enteredWordSummaryColor: Color
   val currentWordDisplay: String
   fun backspace()
   fun append(letter: Char)
   suspend fun enter()
   suspend fun shuffleHoneycomb()
   val enterEnabled: Boolean
   val hasWordHints: Boolean
   val wordHintRevealed: String
   val wordHintSummary: String
   val earnedPointsEvents: Channel<EarnedPointsEvent>
   
   data class EarnedPointsEvent(val points: Short, val toastText: String)
}
