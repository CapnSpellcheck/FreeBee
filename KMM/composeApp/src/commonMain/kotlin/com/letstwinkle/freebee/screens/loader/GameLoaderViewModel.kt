package com.letstwinkle.freebee.screens.loader

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.letstwinkle.freebee.database.FreeBeeRepository
import com.letstwinkle.freebee.gameURL
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.core.use
import kotlinx.datetime.LocalDate
import org.lighthousegames.logging.logging

private val endingNumberRegex = Regex("\\d+$")
private val log = logging()

class GameLoaderViewModel(val gameDate: LocalDate, private val repository: FreeBeeRepository) 
   : ViewModel()
{
   private var statusMutable = mutableStateOf<LoadingStatus>(LoadingStatus.Loading)
   
   val status: State<LoadingStatus>
      get() = statusMutable
   
   suspend fun load() {
      log.d { "load: game date=$gameDate" }
      val gameURL = gameURL(gameDate)
      try {
         val httpClient = HttpClient()
         httpClient.use { 
            val response = it.get(gameURL)
            val html = response.bodyAsText()
            // TODO: not unique handler
            val gameData = parse(html, { _: List<Char> -> ' ' } )
            repository.createGame(
               gameData.date,
               gameData.allowedWords,
               gameData.centerLetterCode,
               gameData.otherLetters,
               gameData.geniusScore,
               gameData.maximumScore
            )
            statusMutable.value = LoadingStatus.Finished
         }
      } catch(error: Throwable) {
         statusMutable.value = LoadingStatus.Error(error)
      }
   }
   
   suspend fun parse(html: String, onCenterLetterNotUnique: suspend (List<Char>) -> Char): GameData {
      statusMutable.value = LoadingStatus.Parsing
      val document = createHTMLDocument(html)
      val root = document.rootElement
      val answerNodes = root.xpathNodes("//*[@id='main-answer-list'][1]/ul/li//text()[not(parent::a)]")
      val puzzleNotes = root.xpathNode("//*[@id='puzzle-notes'][1]")
      val maximumPuzzleScore = puzzleNotes.xpathNode(".//*[contains(., 'Maximum Puzzle Score')][1]")
      val neededForGenius = puzzleNotes.xpathNode(".//*[contains(., 'Needed for Genius')][1]")
      
      val allowedWords = answerNodes.mapNotNull { 
         it.textContent().trim().let { it.ifEmpty { null } }
      }
      val maximumScore = endingNumberRegex.find(maximumPuzzleScore.textContent())?.value?.toShort()
         ?: throw ParseError("Couldn't extract 'Maximum Puzzle Score' (text content was: ${maximumPuzzleScore.textContent()})")
      val geniusScore = endingNumberRegex.find(neededForGenius.textContent())?.value?.toShort()
         ?: throw ParseError("Couldn't extract 'Needed for Genius' (text content was: ${neededForGenius.textContent()})")
      log.d { "Partial parse: allowedWords=$allowedWords maximumScore=$maximumScore geniusScore=$geniusScore" }
      
      var lettersResult = determineLetters(allowedWords)
      if (lettersResult is DetermineLettersResult.NotUnique) {
         val centerLetter = onCenterLetterNotUnique(lettersResult.centerLetterPossibilities)
         lettersResult = determineLetters(allowedWords, centerLetter)
      }
      if (lettersResult is DetermineLettersResult.Irreconcilable) {
         throw ParseError("Couldn't identify the game letters")
      }
      
      lettersResult as DetermineLettersResult.Unique
      return GameData(
         gameDate,
         allowedWords.toSet(),
         lettersResult.centerLetter.code,
         lettersResult.otherLetters,
         geniusScore,
         maximumScore
      )
   }
   
   private fun determineLetters(words: List<String>, centerLetter: Char? = null): 
      DetermineLettersResult 
   {
      val foundLetters = HashSet<Char>(7)
      val centerCandidates = HashSet<Char>(7).apply { addAll(words.first().asIterable()) }
      
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
   data object Finished : LoadingStatus("")
   data class Error(val error: Throwable) : LoadingStatus("Failed")
}

private class ParseError(message: String) : Exception(message)
