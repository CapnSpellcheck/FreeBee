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

private val log = logging()

class GameLoaderViewModel<Id>(
   val gameDate: LocalDate,
   private val repository: FreeBeeRepository<Id, *, *>,
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
         val httpClient = HttpClient()
         httpClient.use { 
            val response = it.get(gameURL)
            val html = response.bodyAsText()
            statusMutable.value = LoadingStatus.Parsing
            val gameData = withContext(Dispatchers.Default) {
               parseGame(gameDate, html, onCenterLetterNotUnique)
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

}

sealed class LoadingStatus(val statusText: String) {
   val showProgress: Boolean
      get() = this is Loading || this is Parsing
   
   data object Loading : LoadingStatus("Downloading…")
   data object Parsing : LoadingStatus("Processing…")
   data class Finished<Id>(val gameID: Id) : LoadingStatus("")
   data class Error(val error: Throwable) : LoadingStatus("Failed")
}

