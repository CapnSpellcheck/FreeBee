package com.letstwinkle.freebee.screens.loader

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.letstwinkle.freebee.*
import com.letstwinkle.freebee.database.*
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.core.use
import kotlinx.coroutines.*
import kotlinx.datetime.LocalDate
import org.lighthousegames.logging.logging
import kotlin.coroutines.CoroutineContext

private val log = logging()
private val standardHTMLParser = object : GameLoaderViewModel.GameHTMLParser {
   override suspend fun parse(date: LocalDate, html: String, onCenterLetterNotUnique: (suspend (List<Char>) -> Char)?) =
      parseGame(date, html, onCenterLetterNotUnique)
}

class GameLoaderViewModel<Id>(
   val gameDate: LocalDate,
   private val httpClientProvider: HttpClientProvider = DefaultHttpClientProvider,
   private val repository: FreeBeeRepository<Id, *, *>,
   private val parsingContext: CoroutineContext = Dispatchers.Default,
   private val parser: GameHTMLParser = standardHTMLParser
): ViewModel()
{
   private var statusMutable = mutableStateOf<LoadingStatus>(LoadingStatus.Loading)

   val status: State<LoadingStatus>
      get() = statusMutable
   
   suspend fun load(onCenterLetterNotUnique: suspend (List<Char>) -> Char) {
      log.d { "load: game date=$gameDate" }

      // for Today quick action
      repository.fetchGame(gameDate)?.let { game ->
         statusMutable.value = LoadingStatus.Finished(game.uniqueID)
         return
      }
      
      val gameURL = gameURL(gameDate)
      try {
         val httpClient = httpClientProvider.provide()
         httpClient.use { 
            val response = it.get(gameURL)
            if (response.status != HttpStatusCode.OK) {
               throw Exception("Failed to download game data")
            }
            val html = response.bodyAsText()
            statusMutable.value = LoadingStatus.Parsing
            val gameData = withContext(parsingContext) {
               parser.parse(gameDate, html, onCenterLetterNotUnique)
            }
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

   interface GameHTMLParser {
      suspend fun parse(
         date: LocalDate,
         html: String,
         onCenterLetterNotUnique: (suspend (List<Char>) -> Char)? = null
      ): GameData
   }
}

sealed class LoadingStatus(val statusText: String) {
   val showProgress: Boolean
      get() = this is Loading || this is Parsing
   
   data object Loading : LoadingStatus("Downloading…")
   data object Parsing : LoadingStatus("Processing…")
   data class Finished<Id>(val gameID: Id) : LoadingStatus("")
   data class Error(val error: Throwable) : LoadingStatus("Failed")
}

