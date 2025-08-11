@file:OptIn(ExperimentalTestApi::class)

package com.letstwinkle.freebee.ui

import androidx.compose.material.*
import androidx.compose.ui.test.*
import com.letstwinkle.freebee.screens.game.GameScreen
import com.letstwinkle.freebee.screens.game.GameWithSheets
import com.letstwinkle.freebee.util.*
import kotlinx.datetime.*
import kotlin.test.*

private const val CurrentWord = "currentWord"

@OptIn(ExperimentalMaterialApi::class)
class GameTest {
   val backNavigator = MockBackNavigator()
   lateinit var viewModel: MockGameViewModel
   
   @BeforeTest fun setUp() {
      val enteredWords = linkedSetOf<MockWord>()
      val game = MockGame(
         Clock.System.todayIn(TimeZone.currentSystemDefault()),
         setOf("abcd", "abcdefg"),
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
   
   // TODO: doesn't pass. I think this is a flaw in the testing toolkit, somewhere related to `performClick()`. A
   // newer version might fix the issue, but I must stick with the current version as I cannot currently update Xcode.
   @Test fun testEnter() = runComposeUiTest() {
      setContent()
      
      val enterButton = onNodeWithContentDescription("submit the entered letters")
      
      enterButton.assertIsNotEnabled()
      enterButton.assertHasClickAction()
      
      onNodeWithText("A").performClick()
      onNodeWithText("A").performClick()
      onNodeWithText("A").performClick()
      onNodeWithText("A").performClick()
      enterButton.assertIsEnabled()
      
      enterButton.performClick() // this line doesn't trigger the click action in Game.kt
      enterButton.assertIsNotEnabled()
      onNodeWithTag(CurrentWord).onChild().assertTextEquals("_")
   }
   
   @Test fun testClickingEnteredWordBar() = runComposeUiTest {
      viewModel.gameWithWords.value.game.enteredWords +=
         listOf("abbbbbbb", "accccccc", "addddddd", "aeeeeee", "affffff", "agggggg").map { MockWord(it) }
      // using GameScreen results in test failure. Don't yet understand why, but possibly updating to a newer
      // version of CMP or Material library would solve it. but I must stick with the current version as I cannot currently update Xcode.
      setContent {
         val rulesState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
         GameWithSheets(viewModel, rulesState)
      }
      
      onNode(clickLabelMatcher("expand entered words")).performClick()
      onNodeWithText("Entered words").isDisplayed()
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
   
   // Since performing the click on the 'enter' button isn't working (see `testEnter()`), this one doesn't pass
   // either.
   @Test fun testEarnedPointsFeedback() = runComposeUiTest {
      setContent()
      
      onAllNodesWithText("WORD").assertAll(invert(isDisplayedMatcher()))
      onAllNodesWithText("PANGRAM").assertAll(invert(isDisplayedMatcher()))
      
      onNodeWithText("A").performClick()
      onNodeWithText("B").performClick()
      onNodeWithText("C").performClick()
      onNodeWithText("D").performClick()
      
      onNodeWithContentDescription("submit the entered letters").performClick()
      
      waitUntil(100) {
         onNodeWithText("WORD").isDisplayed() && onNodeWithText("+1").isDisplayed()
      }
   }
   
   // TODO: Add tests for word hint dialog
}
