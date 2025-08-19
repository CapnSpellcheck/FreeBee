package com.letstwinkle.freebee.unit

import com.letstwinkle.freebee.SettingKeys
import com.letstwinkle.freebee.screens.root.GameListViewModel
import com.letstwinkle.freebee.util.MockGame
import com.letstwinkle.freebee.util.TestRepository
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.lighthousegames.logging.KmLogging
import org.lighthousegames.logging.LogLevel
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class GameListViewModelTest {
   lateinit var viewModel: GameListViewModel<Int, MockGame>
   lateinit var repository: TestRepository
   
   val testDispatcher: TestDispatcher = StandardTestDispatcher()
   
   init {
      KmLogging.setLogLevel(LogLevel.Off)
   }
   
   @BeforeTest
   fun setUp() {
      Dispatchers.setMain(testDispatcher)
      repository = TestRepository()
      runBlocking {
         repository.createGame(
            date = LocalDate.fromEpochDays(0),
            allowedWords = setOf("zabc", "zdef", "zabcdef"),
            centerLetterCode = 'z'.code,
            otherLetters = "abcdef",
            geniusScore = 100,
            maximumScore = 200,
            2,
            scoredAt = Instant.fromEpochSeconds(1755573000)
         )
         repository.createGame(
            date = LocalDate.fromEpochDays(1),
            allowedWords = setOf("zggg", "zdef", "zhhh"),
            centerLetterCode = 'z'.code,
            otherLetters = "defghi",
            geniusScore = 100,
            maximumScore = 200,
            5,
            scoredAt = Instant.fromEpochSeconds(1755572000)
         )
      }
      
      val mockSettings = MapSettings(SettingKeys.OrderGameListByScoredAt to false)
      viewModel = GameListViewModel(repository, mockSettings)
   }
   
   @AfterTest
   fun tearDown() {
      Dispatchers.resetMain()
   }
   
   @Test fun testInitUsesSetting() = runTest {
      assertFalse(viewModel.orderByScored, "setting is false")
      assertContentEquals(listOf(1, 0), viewModel.gamesFlow.value.map { it.uniqueID }, "games are sorted by date")
   }
   
   @OptIn(FlowPreview::class)
   @Test fun testToggleOrderByScored() = runTest {
      viewModel.orderByScored = true
      println("asserting")
      assertContentEquals(
         listOf(0, 1),
         viewModel.gamesFlow.debounce(100L).first().map { it.uniqueID },
         "games are sorted by scoredAt"
      )
   }
}