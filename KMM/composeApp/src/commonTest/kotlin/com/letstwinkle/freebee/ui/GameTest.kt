@file:OptIn(ExperimentalTestApi::class)

package com.letstwinkle.freebee.ui

import androidx.compose.ui.test.*
import com.letstwinkle.freebee.screens.game.GameScreen
import com.letstwinkle.freebee.util.*
import kotlinx.datetime.*
import kotlin.test.*

private const val CurrentWord = "currentWord"
class GameTest {
   val backNavigator = MockBackNavigator()
   lateinit var viewModel: MockGameViewModel
   
   @BeforeTest fun setUp() {
      val enteredWords = linkedSetOf<MockWord>()
      val game = MockGame(
         Clock.System.todayIn(TimeZone.currentSystemDefault()),
         emptySet(),
         'a'.code,
         "bcdefg",
         9,
         10,
         enteredWords
      )
      viewModel = MockGameViewModel(game)
   }
   
   private fun ComposeUiTest.setContent() {
      setContent {
         GameScreen(viewModel, viewModel.gameWithWords.value.game.date, backNavigator)
      }
   }
   
   @Test fun testClickingLetters() = runComposeUiTest {
      setContent()
      
      val currentWordText = onNodeWithTag(CurrentWord).onChild()
      
      currentWordText.assertTextEquals("_")
      
      onNodeWithText("B").performClick()
      currentWordText.assertTextEquals("B_")
      
      onNodeWithText("A").performClick()
      currentWordText.assertTextEquals("BA_")
   }
   
   // TODO: doesn't pass
   @Test fun testClickingEnteredWordBar() = runComposeUiTest {
      viewModel.gameWithWords.value.game.enteredWords +=
         listOf("abbbbbbb", "accccccc", "addddddd", "aeeeeee", "affffff", "agggggg").map { MockWord(it) }
      setContent()
      
      onNode(clickLabelMatcher("expand entered words")).performClick()
      
      waitUntil(100) {
         onNodeWithText("Entered words").isDisplayed()
      }
      
   }
   
   @Test fun testRulesIcon() = runComposeUiTest {
      setContent()
      
      onNodeWithText("Rules").assertIsNotDisplayed()
      onNodeWithContentDescription("rules").performClick()
      onNodeWithText("Rules").assertIsDisplayed()
   }
   
   @Test fun testGeniusIcon() = runComposeUiTest {
      // initially not visible, score = 0
      setContent()
      
      onNodeWithContentDescription("you earned Genius").assertDoesNotExist()
      
      // change the game to genius score
      viewModel.gameWithWords.value.let { it.score = it.geniusScore }
      // reload the screen so the VM gets genius status
      setContent()
      
      onNodeWithContentDescription("you earned Genius").assertExists()
      onNodeWithContentDescription("you earned Genius").performClick()
      onNodeWithText("You reached genius", substring = true).assertExists()
   }
   
   @Test fun testBack() = runComposeUiTest {
      setContent()
      
      onNodeWithText("Back", substring = true).performClick()
      assertEquals(1, backNavigator.goBackCount)
   }
   
   @Test fun testShuffle() = runComposeUiTest {
      setContent()
      val otherLetters = viewModel.gameWithWords.value.game.otherLetters
      
      onNodeWithContentDescription("shuffle honeycomb").performClick()
      assertNotEquals(otherLetters, viewModel.gameWithWords.value.game.otherLetters, "shuffle otherLetters")
   }
}
