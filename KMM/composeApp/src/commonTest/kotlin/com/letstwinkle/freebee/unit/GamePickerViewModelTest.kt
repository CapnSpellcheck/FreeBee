@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("MemberVisibilityCanBePrivate")

package com.letstwinkle.freebee.unit

import com.letstwinkle.freebee.HttpClientProvider
import com.letstwinkle.freebee.screens.picker.GamePickerViewModel
import com.letstwinkle.freebee.util.BetterMockEngine
import com.letstwinkle.freebee.util.TestRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.lighthousegames.logging.KmLogging
import org.lighthousegames.logging.LogLevel
import kotlin.test.*

class GamePickerViewModelTest {
   val mockClient: HttpClient
   val todaysDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
   val mockClientProvider: HttpClientProvider
   
   lateinit var viewModel: GamePickerViewModel
   lateinit var repository: TestRepository
   var latestAvailableDate: LocalDate = todaysDate
   
   private val testDispatcher: TestDispatcher = StandardTestDispatcher()
   
   init {
      KmLogging.setLogLevel(LogLevel.Off)
      val mockEngineConfig = MockEngineConfig()
      mockEngineConfig.addHandler { request ->
         println("< MOCK START")
         val requestDate = LocalDate.parse(
            request.url.fullPath.removeSuffix(".html").removePrefix("/Bee_"),
            LocalDate.Formats.ISO_BASIC
         )
         println("  MOCK DATE: $requestDate")
         if (requestDate <= latestAvailableDate) {
            println("  MOCK 200")
            
            respondOk()
         } else {
            println("  MOCK 404")
            
            respondError(HttpStatusCode.NotFound)
         }
      }
      mockClient = HttpClient(BetterMockEngine(testDispatcher, mockEngineConfig))
      mockClientProvider = object : HttpClientProvider {
         override fun provide(): HttpClient = mockClient
      }
   }
   
   private fun insertGame(daysAgo: Int) {
      runBlocking {
         repository.createGame(
            date = todaysDate - DatePeriod(days = daysAgo),
            allowedWords = setOf(),
            centerLetterCode = 'z'.code,
            otherLetters = "abcdef",
            geniusScore = 100,
            maximumScore = 200,
         )
      }
   }
   
   @BeforeTest fun setUp() {
      Dispatchers.setMain(testDispatcher)
      repository = TestRepository()
   }
   
   
   @AfterTest fun tearDown() {
      Dispatchers.resetMain()
   }
   
   @Test fun testSelectedDateValueOnInit_1() = runTest(testDispatcher) {
      latestAvailableDate = todaysDate
      viewModel = GamePickerViewModel(repository, mockClientProvider)
      advanceUntilIdle()
      assertEquals(todaysDate, viewModel.selectedDate.value, "view model init, no games saved, latest available date = today")
   }
   
   @Test fun testSelectedDateValueOnInit_2() = runTest(testDispatcher) {
      latestAvailableDate = todaysDate
      insertGame(0)
      insertGame(1)
      viewModel = GamePickerViewModel(repository, mockClientProvider)
      advanceUntilIdle()
      assertEquals(todaysDate - DatePeriod(days = 2), viewModel.selectedDate.value, "view model init, today's and yesterdays games are saved, latest available date = today")
   }

   @Test fun testSelectedDateValueOnInit_3() = runTest(testDispatcher) {
      latestAvailableDate = todaysDate - DatePeriod(days = 3)
      viewModel = GamePickerViewModel(repository, mockClientProvider)
      advanceUntilIdle()
      assertEquals(todaysDate - DatePeriod(days = 3), viewModel.selectedDate.value, "view model init, no games  saved, latest available date = 3 days before today")
   }
}
