package com.letstwinkle.freebee.screens.picker

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.letstwinkle.freebee.database.FreeBeeRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.http.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import org.lighthousegames.logging.logging
import kotlin.random.Random
import kotlin.random.nextInt

private val earliestGameDate = LocalDate(2018, 8, 1)
private val log = logging()

class GamePickerViewModel(
   private val repository: FreeBeeRepository,
) : ViewModel() {
   private val selectedDateMutable: MutableState<LocalDate>
   private val isChoosingRandomDateMutable = mutableStateOf(false)
   
   private var latestAvailableDate: LocalDate
   
   val selectedDate: State<LocalDate>
      get() = selectedDateMutable
   val isChoosingRandomDate: State<Boolean>
      get() = isChoosingRandomDateMutable
   
   init {
      val todaysDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
      latestAvailableDate = todaysDate
      selectedDateMutable = mutableStateOf(todaysDate)
      viewModelScope.launch {
         determineLatestAvailableDate()
         selectedDateMutable.value = latestAvailableDate
      }
   }
   
   @OptIn(ExperimentalMaterial3Api::class)
   // Below init for the reference to 'latestAvailableDate'
   val selectableDates = object : SelectableDates {
      val thisYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
      
      override fun isSelectableDate(utcTimeMillis: Long): Boolean {
         log.d { "isSelectableDate($utcTimeMillis) "}
         val date = Instant.fromEpochMilliseconds(utcTimeMillis)
            .toLocalDateTime(TimeZone.UTC).date
         return !isGameLoaded(date) && date <= latestAvailableDate && date >= earliestGameDate
      }
      
      override fun isSelectableYear(year: Int): Boolean {
         return year >= earliestGameDate.year && year <= thisYear
      }
   }
   
   fun updateSelectedDate(timestampWithMillis: Long?) {
      timestampWithMillis?.let {
         selectedDateMutable.value = Instant.fromEpochMilliseconds(timestampWithMillis)
            .toLocalDateTime(TimeZone.UTC).date
      }
   }
   
   suspend fun chooseRandomDate() {
      isChoosingRandomDateMutable.value = true
      val validDayRange = IntRange(earliestGameDate.toEpochDays(), latestAvailableDate.toEpochDays())
      var randomDate: LocalDate
      
      do {
         randomDate = LocalDate.fromEpochDays(Random.Default.nextInt(validDayRange))
         log.d { "chooseRandomDate: range=$validDayRange randomDate=$randomDate" }
      } while (isGameLoaded(randomDate))
      selectedDateMutable.value = randomDate
   }
   
   private suspend fun determineLatestAvailableDate() {
      var checkDate = latestAvailableDate
      val httpClient = HttpClient()
      val gameURLBuilder = URLBuilder(host = "nytbee.com", protocol = URLProtocol.HTTPS)
      
      while (checkDate >= earliestGameDate) {
         log.d { "determineLatestAvailableDate(): checking date $checkDate"}
         // if the game is saved locally, skip it
         if (!isGameLoaded(checkDate)) {
            // check whether the website responds with 404 for the date.
            gameURLBuilder.set(path = "Bee_${checkDate.format(LocalDate.Formats.ISO_BASIC)}.html")
            try {
               val response = httpClient.head(gameURLBuilder.build())
               if (response.status.isSuccess()) {
                  log.d { "determineLatestAvailableDate(): HEAD success" }
                  latestAvailableDate = checkDate
                  break
               } else {
                  log.d { "determineLatestAvailableDate(): FAIL: $response" }
               }
            } catch (throwable: Throwable) {
               log.i { "httpClient threw: ${throwable.message}" }
            }
         }
         
         checkDate = checkDate.minus(1, DateTimeUnit.DAY)
      }
   }
   
   private fun isGameLoaded(date: LocalDate): Boolean {
      log.d { "Checking for existence of game with date = $date" }
      
      return repository.hasGameForDate(date)
   }
}
