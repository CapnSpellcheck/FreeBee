@file:OptIn(ExperimentalTestApi::class)

package com.letstwinkle.freebee.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.letstwinkle.freebee.screens.root.GameListHeader
import com.letstwinkle.freebee.screens.root.GameListScreen
import com.letstwinkle.freebee.util.*
import kotlinx.datetime.*
import kotlin.test.Test
import kotlin.test.assertEquals

private const val OpenThisGame = "Open this game"
private const val ChooseANewGame = "Choose a new game"

class GameListTest {
   val repository = TestRepository()
   val navigator = MockGameListNavigator()
   val owner = object : ViewModelStoreOwner {
      override val viewModelStore: ViewModelStore = ViewModelStore()
   }
   
   @Test fun testNoGamesLayout() = runComposeUiTest {
      // repository is empty
      setContent { 
         CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            GameListScreen(repository, navigator)
         }
      }
      
      onNodeWithText(GameListHeader.InProgress.label).assertExists()
      onNodeWithText(GameListHeader.Completed.label).assertDoesNotExist()
      onNodeWithText(GameListHeader.New.label).assertExists()
      onNodeWithText(ChooseANewGame).assertExists()
      onAllNodesWithTag("divider").assertCountEquals(0)
      onAllNodes(clickLabelMatcher(OpenThisGame)).assertCountEquals(0)
   }
   
   @Test fun testOneCompletedGameLayout() = runComposeUiTest {
      repository.createGame(Clock.System.todayIn(TimeZone.currentSystemDefault()), emptySet(), 'a'.code, "bcdefg", 9, 10, 10)
      
      setContent {
         CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            GameListScreen(repository, navigator)
         }
      }
      
      onNodeWithText(GameListHeader.InProgress.label).assertExists()
      onNodeWithText(GameListHeader.Completed.label).assertExists()
      onNodeWithText(GameListHeader.New.label).assertExists()
      onNodeWithText(ChooseANewGame).assertExists()
      onAllNodesWithTag("divider").assertCountEquals(2)
      onAllNodes(clickLabelMatcher(OpenThisGame)).assertCountEquals(1)
      onNodeWithText(GameListHeader.Completed.label).also {
         val completedHeaderNode = it.fetchSemanticsNode()
         val siblingNodes = it.onParent().onChildren().fetchSemanticsNodes()
         val completedHeaderNodeIndex = siblingNodes.indexOfFirst { it.config == completedHeaderNode.config }
         assertEquals(
            "divider",
            siblingNodes[completedHeaderNodeIndex + 1].config.getOrNull(SemanticsProperties.TestTag),
            "The node after the Complete header is a divider"
         )
         assertEquals(
            OpenThisGame,
            siblingNodes[completedHeaderNodeIndex + 2].config.getOrNull(SemanticsActions.OnClick)?.label,
            "The second node after the Complete header is a clickable game"
         )
         assertEquals(
            "divider",
            siblingNodes[completedHeaderNodeIndex + 3].config.getOrNull(SemanticsProperties.TestTag),
            "The third node after the Complete header is a divider"
         )
      }
      
   }
   
   @Test fun test2InProgressGamesLayout() = runComposeUiTest {
      repository.createGame(
         Clock.System.todayIn(TimeZone.currentSystemDefault()),
         emptySet(),
         'a'.code,
         "bcdefg",
         9,
         10,
         0
      )
      repository.createGame(
         Clock.System.todayIn(TimeZone.currentSystemDefault()) - DatePeriod(days = 1),
         emptySet(),
         'a'.code,
         "bcdefg",
         9,
         10,
         5
      )
      
      setContent {
         CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            GameListScreen(repository, navigator)
         }
      }
      
      onNodeWithText(GameListHeader.InProgress.label).assertExists()
      onNodeWithText(GameListHeader.Completed.label).assertDoesNotExist()
      onNodeWithText(GameListHeader.New.label).assertExists()
      onNodeWithText(ChooseANewGame).assertExists()
      onAllNodesWithTag("divider").assertCountEquals(2)
      onAllNodes(clickLabelMatcher(OpenThisGame)).assertCountEquals(2)
      onNodeWithText(GameListHeader.InProgress.label).also {
         val inProgressHeaderNode = it.fetchSemanticsNode()
         val siblingNodes = it.onParent().onChildren().fetchSemanticsNodes()
         val inProgressHeaderNodeIndex =
            siblingNodes.indexOfFirst { it.config == inProgressHeaderNode.config }
         assertEquals(
            "divider",
            siblingNodes[inProgressHeaderNodeIndex + 1].config.getOrNull(SemanticsProperties.TestTag),
            "The node after the InProgress header is a divider"
         )
         assertEquals(
            OpenThisGame,
            siblingNodes[inProgressHeaderNodeIndex + 2].config.getOrNull(SemanticsActions.OnClick)?.label,
            "The second node after the InProgress header is a clickable game"
         )
         assertEquals(
            OpenThisGame,
            siblingNodes[inProgressHeaderNodeIndex + 3].config.getOrNull(SemanticsActions.OnClick)?.label,
            "The third node after the InProgress header is a clickable game"
         )
         assertEquals(
            "divider",
            siblingNodes[inProgressHeaderNodeIndex + 4].config.getOrNull(SemanticsProperties.TestTag),
            "The fourth node after the InProgress header is a divider"
         )
      }
   }
   
   @Test fun testNewGame() = runComposeUiTest {
      repository.createGame(Clock.System.todayIn(TimeZone.currentSystemDefault()), emptySet(), 'a'.code, "bcdefg", 9, 10, 10)
      
      setContent {
         CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            GameListScreen(repository, navigator)
         }
      }
      
      onNodeWithText(ChooseANewGame).performClick()
      assertEquals(1, navigator.openGamePickerCount)
      assertEquals(0, navigator.openGameCount)
      assertEquals(0, navigator.showStatisticsCount)
   }
   
   @Test fun testOpenGame() = runComposeUiTest {
      repository.createGame(Clock.System.todayIn(TimeZone.currentSystemDefault()), emptySet(), 'a'.code, "bcdefg", 9, 10, 10)
      
      setContent {
         CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            GameListScreen(repository, navigator)
         }
      }
      
      onNode(clickLabelMatcher(OpenThisGame)).performClick()
      assertEquals(0, navigator.openGamePickerCount)
      assertEquals(1, navigator.openGameCount)
      assertEquals(0, navigator.showStatisticsCount)
      assertEquals(repository.games[0], navigator.lastGameOpened)
   }
   
   @Test fun testOpenStatistics() = runComposeUiTest {
      setContent {
         CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            GameListScreen(repository, navigator)
         }
      }
      
      onNodeWithContentDescription("Statistics").performClick()
      assertEquals(1, navigator.showStatisticsCount)
   }
}
