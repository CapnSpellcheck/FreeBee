package com.letstwinkle.freebee.screens.loader

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.letstwinkle.freebee.database.*
import com.letstwinkle.freebee.gameURL
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.core.use
import kotlinx.coroutines.*
import kotlinx.datetime.LocalDate
import org.lighthousegames.logging.logging

private val endingNumberRegex = Regex("\\d+$")
private val log = logging()

class GameLoaderViewModel<Id>(
   val gameDate: LocalDate,
   private val repository: FreeBeeRepository<Id, *, *>,
) 
   : ViewModel()
{
   private var statusMutable = mutableStateOf<LoadingStatus>(LoadingStatus.Loading)
   private var onCenterLetterNotUnique: (suspend (List<Char>) -> Char)? = null
   
   val status: State<LoadingStatus>
      get() = statusMutable
   
   suspend fun load(onCenterLetterNotUnique: suspend (List<Char>) -> Char) {
      log.d { "load: game date=$gameDate" }
      this.onCenterLetterNotUnique = onCenterLetterNotUnique
      
      // for Today quick action
      repository.fetchGame(gameDate)?.let { game ->
         statusMutable.value = LoadingStatus.Finished(game.uniqueID)
         return
      }
      
      val gameURL = gameURL(gameDate)
      try {
         val httpClient = HttpClient()
         httpClient.use { 
            val response = it.get(gameURL)
            val html = response.bodyAsText()
            val gameData = parse(html)
            val gameID = repository.createGame(
               gameData.date,
               gameData.allowedWords,
               gameData.centerLetterCode,
               gameData.otherLetters,
               gameData.geniusScore,
               gameData.maximumScore
            )
            statusMutable.value = LoadingStatus.Finished(gameID)
         }
      } catch(error: Throwable) {
         statusMutable.value = LoadingStatus.Error(error)
      }
   }
   
   suspend fun parse(html: String): GameData {
      return withContext(Dispatchers.Default) {
         statusMutable.value = LoadingStatus.Parsing
         val document = createHTMLDocument(html)
         val root = document.rootElement
         var answerList = root.xpathNode("//*[@id='main-answer-list'][1]")
         val puzzleNotes: Node?
         if (answerList != null) {
            puzzleNotes = root.xpathNode("//*[@id='puzzle-notes'][1]")
         } else {
            answerList = root.xpathNode("//*[@class='answer-list'][1]")
            puzzleNotes = answerList?.xpathNode("./following-sibling::div[1]")
         }
         
         if (answerList == null) 
            throw ParseError("Couldn't find answer list")
         if (puzzleNotes == null)
            throw ParseError("Couldn't find scoring information")

         val answerNodes = answerList.xpathNodes("./ul/li//text()[not(parent::a)]")
         val allowedWords = answerNodes.mapNotNull {
            it.textContent().trim().let { it.ifEmpty { null } }
         }
         
         val maximumPuzzleScore =
            puzzleNotes.xpathNode(".//*[contains(., 'Maximum Puzzle Score')][1]")
               ?: throw ParseError("Couldn't find Maximum Puzzle Score")
         val neededForGenius = puzzleNotes.xpathNode(".//*[contains(., 'Needed for Genius')][1]")
            ?: throw ParseError("Couldn't find Needed for Genius")
         
         val maximumScore =
            endingNumberRegex.find(maximumPuzzleScore.textContent())?.value?.toShort()
               ?: throw ParseError("Couldn't extract Maximum Puzzle Score (text content was: ${maximumPuzzleScore.textContent()})")
         val geniusScore = endingNumberRegex.find(neededForGenius.textContent())?.value?.toShort()
            ?: throw ParseError("Couldn't extract Needed for Genius (text content was: ${neededForGenius.textContent()})")
         
         var lettersResult = determineLetters(allowedWords)
         if (lettersResult is DetermineLettersResult.NotUnique) {
            val centerLetter =
               onCenterLetterNotUnique?.invoke(lettersResult.centerLetterPossibilities)
            lettersResult = determineLetters(allowedWords, centerLetter)
         }
         if (lettersResult is DetermineLettersResult.Irreconcilable) {
            throw ParseError("Couldn't identify the game letters")
         }
         
         lettersResult as DetermineLettersResult.Unique
         GameData(
            gameDate,
            allowedWords.toSet(),
            lettersResult.centerLetter.code,
            lettersResult.otherLetters,
            geniusScore,
            maximumScore
         )
      }
   }
   
   private fun determineLetters(words: List<String>, centerLetter: Char? = null)
      : DetermineLettersResult 
   {
      val foundLetters = HashSet<Char>(7)
      val centerCandidates = HashSet<Char>(7).apply {
         if (centerLetter != null)
            add(centerLetter)
         else
            addAll(words.first().asIterable())
      }
      
      for (word in words) {
         foundLetters.addAll(word.asIterable())
         centerCandidates.retainAll(word.toSet())
         if (foundLetters.size == 7 && (centerLetter != null || centerCandidates.size == 1))
            break
      }
      
      if (foundLetters.size != 7)
         return DetermineLettersResult.Irreconcilable
      if (centerCandidates.size != 1)
         return DetermineLettersResult.NotUnique(centerCandidates.toList())
      
      val centerLetter = centerLetter ?: centerCandidates.first()
      foundLetters.remove(centerLetter)
      return DetermineLettersResult.Unique(centerLetter, foundLetters.joinToString(""))
   }
}

sealed class DetermineLettersResult {
   data class Unique(val centerLetter: Char, val otherLetters: String) : DetermineLettersResult()
   data class NotUnique(val centerLetterPossibilities: List<Char>) : DetermineLettersResult()
   data object Irreconcilable : DetermineLettersResult()
}

data class GameData(
   val date: LocalDate,
   val allowedWords: Set<String>,
   val centerLetterCode: Int,
   val otherLetters: String,
   val geniusScore: Short,
   val maximumScore: Short
)

sealed class LoadingStatus(val statusText: String) {
   val showProgress: Boolean
      get() = this is Loading || this is Parsing
   
   data object Loading : LoadingStatus("Downloading…")
   data object Parsing : LoadingStatus("Processing…")
   data class Finished<Id>(val gameID: Id) : LoadingStatus("")
   data class Error(val error: Throwable) : LoadingStatus("Failed")
}

private class ParseError(message: String) : Exception(message)
