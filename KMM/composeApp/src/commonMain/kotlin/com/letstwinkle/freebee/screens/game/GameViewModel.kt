package com.letstwinkle.freebee.screens.game

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.letstwinkle.freebee.SettingKeys
import com.letstwinkle.freebee.database.*
import com.russhwolf.settings.Settings
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

private val log = logging()
private val MinWordLength = 4

class GameViewModel<GameWithWords: IGameWithWords>(
   private val repository: FreeBeeRepository<out IGame, GameWithWords>,
   private val gameID: EntityIdentifier,
) : ViewModel() {
   private val gameWithWordsMutable = mutableStateOf<GameWithWords?>(null, policy = neverEqualPolicy())
   private val entryNotAcceptedMessageMutable = mutableStateOf<String?>(null)
   val gameWithWords: State<GameWithWords?>
      get() = gameWithWordsMutable
   val entryNotAcceptedMessage: State<String?>
      get() = entryNotAcceptedMessageMutable
   
   val gameProgress: Float
      get() = gameWithWords.value?.run { (0f + enteredWords.size) / game.allowedWords.size } ?: 0f
   
   val enterEnabled: Boolean
      get() = gameWithWords.value?.game?.let { 
         it.currentWord.length >= MinWordLength && it.currentWord.contains(it.centerLetterCharacter)
      } ?: false
   
   // Remember the enteredWords are stored oldest first, but display most recent first here.
   val enteredWordSummary: String
      get() {
         val estimateNumberOfCharactersThatCouldBeShown = 60
         var length = 0; var taken = 0
         val enteredWords = gameWithWords.value?.enteredWords
         return enteredWords?.toList()?.asReversed()?.takeWhile {
            length += it.value.length + 2
            if (length <= 60)
               taken++
            length <= 60
         }?.joinToString(
            "\u2003",
            postfix = if (taken < enteredWords.size) "…" else ""
         ) { it.value.replaceFirstChar(Char::titlecase) }
            ?: "No words yet"
      }
   
   val enteredWordSummaryColor: Color
      get() = Color.Black
   
   init {
      viewModelScope.launch {
         gameWithWordsMutable.value = repository.fetchGameWithWords(gameID)
      }
   }
   
   suspend fun enter() {
      var success = true
      val gameWithWords = gameWithWords.value ?: return
      val enteredWord = gameWithWords.game.currentWord
      val wordIsAllowed = gameWithWords.isAllowed(enteredWord)
      val wordIsEntered = gameWithWords.hasEntered(enteredWord)
      
      if (wordIsAllowed && !wordIsEntered) {
         val updatedScore = (gameWithWords.game.score + scoreWord(enteredWord)).toShort()
         success = repository.executeAndSave {
            repository.updateGameScore(gameWithWords, updatedScore)
            repository.addEnteredWord(gameWithWords, enteredWord)
         }
         if (success && gameWithWords.game.isPangram(enteredWord)) {
            log.d { "Pangram entered: $enteredWord" }
            Settings().also {
               it.putInt(
                  SettingKeys.PangramCount,
                  it.getInt(SettingKeys.PangramCount, 0) + 1
               )
               log.d { "Total pangrams: ${it.getInt(SettingKeys.PangramCount, 0)}" }
            }
         }
      } else {
         entryNotAcceptedMessageMutable.value =
            if (wordIsEntered) "Word is already entered"
            else "Entry isn't accepted"
      }
      
      if (success) {
         gameWithWords.game.currentWord = ""
         gameWithWordsMutable.value = gameWithWords
      } else {
         entryNotAcceptedMessageMutable.value = "Error performing operation, sorry"
      }
   }
   
   fun backspace() {
      val gameWithWords = gameWithWords.value ?: return
      gameWithWords.game.apply {
         currentWord = currentWord.dropLast(1)
      }
      gameWithWordsMutable.value = gameWithWords
   }
   
   // temporary, until the LetterHoneycomb is implemented
   fun updateCurrentWord(new: String) {
      val gameWithWords = gameWithWordsMutable.value
      gameWithWords?.game?.currentWord = new
      gameWithWordsMutable.value = gameWithWords
   }
   
   private fun scoreWord(word: String): Short {
      var score = if (word.length == 4) 1 else word.length
      gameWithWords.value?.game?.let { 
         if (it.isPangram(word))
            score += 7
      }
      return score.toShort()
   }
   
}
