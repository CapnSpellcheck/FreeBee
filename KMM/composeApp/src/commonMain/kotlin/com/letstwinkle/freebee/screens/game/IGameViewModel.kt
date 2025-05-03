package com.letstwinkle.freebee.screens.game

import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import com.letstwinkle.freebee.database.IGameWithWords
import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.LocalDate

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
   val wordHints: State<Map<String, LocalDate>>
}

val IGameViewModel<*>.scoreText: AnnotatedString
   get() = buildAnnotatedString {
      append("Score:  ")
      gameWithWords.value?.game?.let {
         pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
         append(it.score.toString())
      }
   }