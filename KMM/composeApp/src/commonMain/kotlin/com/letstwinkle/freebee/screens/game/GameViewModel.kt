package com.letstwinkle.freebee.screens.game

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.letstwinkle.freebee.SettingKeys
import com.letstwinkle.freebee.database.*
import com.letstwinkle.freebee.secondaryTextColor
import com.russhwolf.settings.Settings
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.lighthousegames.logging.logging

private val log = logging()
private const val minWordLength = 4
private const val maxWordLength = 19
private const val enteredWordSpacer = "\u2003"

class GameViewModel<Id, Game: IGame<Id>, GameWithWords: IGameWithWords<Id>>(
   private val repository: FreeBeeRepository<Id, Game, GameWithWords>,
   private val gameID: Id,
   private val multiplatformSettings: Settings = Settings(),
) : ViewModel()
{
   private val gameWithWordsMutable = mutableStateOf<GameWithWords?>(null, policy = neverEqualPolicy())
   /**
    * This is calculated when the game is loaded by joining the initial entered words, then
    * updated when the user correctly enters another word.
    */
   private var joinedEnteredWords = ""
   
   val gameWithWords: State<GameWithWords?>
      get() = gameWithWordsMutable
   val entryNotAcceptedEvents = Channel<String>()
   
   val gameProgress: Float
      get() = gameWithWords.value?.run { (0f + enteredWords.size) / game.allowedWords.size } ?: 0f
   
   val enterEnabled: Boolean
      get() = gameWithWords.value?.game?.let { 
         it.currentWord.length >= minWordLength && it.currentWord.contains(it.centerLetterCharacter)
      } ?: false
   
   // Remember the enteredWords are stored oldest first, but display most recent first here.
   val enteredWordSummary: String
      get() = 
         if (gameWithWords.value?.enteredWords?.isEmpty() == true) "No words yet" 
      else joinedEnteredWords
   
   val enteredWordSummaryColor: Color
      get() = if (gameWithWords.value?.enteredWords.isNullOrEmpty())
         secondaryTextColor
      else Color.Black
   
   val currentWordDisplay: String 
      get() = gameWithWords.value?.game?.let {
         if (it.isComplete) "" else it.currentWordDisplay
      } ?: ""
   
   init {
      viewModelScope.launch {
         val fetchedGameWithWords = repository.fetchGameWithWords(gameID)
         gameWithWordsMutable.value = fetchedGameWithWords
         joinedEnteredWords = fetchedGameWithWords.enteredWords
            .toList().asReversed()
            .joinToString(enteredWordSpacer) { it.value.replaceFirstChar(Char::titlecaseChar)}
      }
   }
   
   suspend fun enter() {
      var committed = false
      var errored = false
      val gameWithWords = gameWithWords.value ?: return
      val enteredWord = gameWithWords.game.currentWord
      val wordIsAllowed = gameWithWords.isAllowed(enteredWord)
      val wordIsEntered = gameWithWords.hasEntered(enteredWord)
      
      if (wordIsAllowed && !wordIsEntered) {
         val updatedScore = (gameWithWords.game.score + scoreWord(enteredWord)).toShort()
         committed = repository.executeAndSave {
            repository.updateGameScore(gameWithWords, updatedScore)
            repository.addEnteredWord(gameWithWords, enteredWord)
         }
         errored = !committed
      } else {
         entryNotAcceptedEvents.send(
            if (wordIsEntered) "Word is already entered"
            else "Entry isn't accepted"
         )
      }
      
      if (committed) {
         if (gameWithWords.game.isPangram(enteredWord)) {
            log.d { "Pangram entered: $enteredWord" }
            recordPangram()
         }
         val enteredWordCapitalized = enteredWord.replaceFirstChar(Char::titlecaseChar)
         joinedEnteredWords = enteredWordCapitalized + 
            if (joinedEnteredWords.isNotEmpty()) enteredWordSpacer + joinedEnteredWords
            else ""
      }
      if (!errored) {
         gameWithWords.game.currentWord = ""
         gameWithWordsMutable.value = gameWithWords
      } else {
         entryNotAcceptedEvents.send("Error performing operation, sorry")
      }
   }
   
   fun backspace() {
      val gameWithWords = gameWithWords.value ?: return
      gameWithWords.game.apply {
         currentWord = currentWord.dropLast(1)
      }
      gameWithWordsMutable.value = gameWithWords
   }
   
   fun append(letter: Char) {
      gameWithWords.value?.game?.currentWord?.let {
         if (it.length < maxWordLength) {
            gameWithWords.value?.game?.currentWord = it + letter
            gameWithWordsMutable.value = gameWithWords.value
         }
      }
   }
   
   private fun scoreWord(word: String): Short {
      var score = if (word.length == 4) 1 else word.length
      gameWithWords.value?.game?.let { 
         if (it.isPangram(word))
            score += 7
      }
      return score.toShort()
   }
   
   private fun recordPangram() {
      multiplatformSettings.putInt(
         SettingKeys.PangramCount,
         multiplatformSettings.getInt(SettingKeys.PangramCount, 0) + 1
      )
      log.d { "Total pangrams: ${multiplatformSettings.getInt(SettingKeys.PangramCount, 0)}" }
   }
}
