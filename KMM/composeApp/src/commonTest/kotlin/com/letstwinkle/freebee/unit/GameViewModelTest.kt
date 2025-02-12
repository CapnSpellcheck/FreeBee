@file:Suppress("MemberVisibilityCanBePrivate")

package com.letstwinkle.freebee.unit

import com.letstwinkle.freebee.screens.game.GameViewModel
import com.letstwinkle.freebee.util.MockGame
import com.letstwinkle.freebee.util.TestRepository
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDate
import org.lighthousegames.logging.KmLogging
import org.lighthousegames.logging.LogLevel
import kotlin.test.*

private typealias TestGameViewModel = GameViewModel<Unit, MockGame, MockGame>

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {
   lateinit var viewModel: TestGameViewModel
   lateinit var repository: TestRepository
   
   val testDispatcher: TestDispatcher = StandardTestDispatcher()
   
   init {
      KmLogging.setLogLevel(LogLevel.Off)
   }
   
   @BeforeTest fun setUp() {
      Dispatchers.setMain(testDispatcher)
      repository = TestRepository()
      val gameID = runBlocking {
         repository.createGame(
            date = LocalDate.fromEpochDays(0),
            allowedWords = setOf("zabc", "zdef", "zabcdef"),
            centerLetterCode = 'z'.code,
            otherLetters = "abcdef",
            geniusScore = 100,
            maximumScore = 200,
         )
      }
      viewModel = GameViewModel(repository, gameID, multiplatformSettings = MapSettings())
   }
   
   @AfterTest fun tearDown() {
      Dispatchers.resetMain()
   }
   
   val gameWithWords: MockGame
      get() = viewModel.gameWithWords.value!!
   
   @Test fun testEnteredWordSummary() = runTest {
      assertEquals("No words yet", viewModel.enteredWordSummary, "Entered word summary when enteredWordCount = 0")
      
      gameWithWords.game.currentWord = "zabc"
      viewModel.enter()
      gameWithWords.game.currentWord = "zdef"
      viewModel.enter()
      
      assertEquals("Zdef\u2003Zabc", viewModel.enteredWordSummary, "Entered word summary when \"zabc\" and \"zdef\" are the entered words")
   }
   
   @Test fun testEnterEnabled() = runTest {
      val game = gameWithWords.game
      
      assertFalse(viewModel.enterEnabled, "enter disabled: currentWord is empty")
      
      game.currentWord = "abz"
      assertFalse(viewModel.enterEnabled, "enter disabled: currentWord too short")
      
      game.currentWord = "abcde"
      assertFalse(viewModel.enterEnabled, "enter disabled: currentWord missing center letter")
      
      game.currentWord = "abcdz"
      assertTrue(viewModel.enterEnabled, "enter enabled")
   }
   
   @Test fun testGameProgress() = runTest {
      assertEquals(0f, viewModel.gameProgress, absoluteTolerance = 0.001f, "game progress is 0")
      
      repository.addEnteredWord(gameWithWords, "zabc")
      assertEquals(0.333f, viewModel.gameProgress, absoluteTolerance = 0.001f, "game progress is 0.3…")
      
      repository.addEnteredWord(gameWithWords, "zdef")
      repository.addEnteredWord(gameWithWords, "zabcdef")
      assertEquals(1.0f, viewModel.gameProgress, absoluteTolerance = 0.001f, "game progress is 1.0")
   }
   
   @Test fun testAppend() = runTest {
      viewModel.append('a')
      assertEquals("a", gameWithWords.game.currentWord, "currentWord is \"a\"")
      
      viewModel.append('z')
      assertEquals("az", gameWithWords.game.currentWord, "currentWord is \"az\"")
      
      val tooLongWord = "a".repeat(50)
      gameWithWords.game.currentWord = tooLongWord
      viewModel.append('a')
      assertEquals(tooLongWord, gameWithWords.game.currentWord, "currentWord doesn't change - max length")
   }
   
   @Test fun testBackspace() = runTest {
      viewModel.backspace()
      assertEquals("", gameWithWords.game.currentWord, "backspace empty")
      
      gameWithWords.game.currentWord = "abcde"
      viewModel.backspace()
      assertEquals("abcd", gameWithWords.game.currentWord, "backspace not empty")
   }
   
   @Test fun testEnter() = runTest {
      backgroundScope.launch { 
         viewModel.entryNotAcceptedEvents.consumeEach {}
      }
      
      gameWithWords.game.currentWord = "abcde"
      viewModel.enter()
      assertEquals(0, gameWithWords.game.score, "testEnter: word not accepted: score")
      assertEquals(0, gameWithWords.enteredWords.size, "testEnter: word not accepted: enteredWords")
      assertEquals("", gameWithWords.game.currentWord, "testEnter: word not accepted: currentWord reset")
      
      gameWithWords.game.currentWord = "zabcdef"
      viewModel.enter()
      assertEquals(14, gameWithWords.game.score, "testEnter: word accepted (pangram): score")
      assertEquals(1, gameWithWords.enteredWords.size, "testEnter: word accepted (pangram): enteredWords")
      assertEquals("", gameWithWords.game.currentWord, "testEnter: word not accepted: currentWord reset")
      
      gameWithWords.game.currentWord = "zabcdef"
      viewModel.enter()
      assertEquals(14, gameWithWords.game.score, "testEnter: word not accepted, repeat: score")
      assertEquals(1, gameWithWords.enteredWords.size, "testEnter: word not accepted, repeat: enteredWords")
      assertEquals("", gameWithWords.game.currentWord, "testEnter: word not accepted, repeat: currentWord reset")
   }
   
   @OptIn(ExperimentalCoroutinesApi::class)
   @Test fun testEntryNotAcceptedEvent() = runTest {
      launch {
         val event = viewModel.entryNotAcceptedEvents.receive()
         assertEquals("Entry isn't accepted", event, "currentWord invalid - receive event")
      }
      gameWithWords.game.currentWord = "zbcd"
      viewModel.enter()
      
      gameWithWords.game.currentWord = "zabc"
      viewModel.enter()
      assertTrue(viewModel.entryNotAcceptedEvents.isEmpty, "currentWord valid - no event")
   }
}
