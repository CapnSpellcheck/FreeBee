@file:OptIn(ExperimentalTestApi::class)

package com.letstwinkle.freebee.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.*
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.letstwinkle.freebee.HttpClientProvider
import com.letstwinkle.freebee.screens.picker.GamePicker
import com.letstwinkle.freebee.screens.picker.GamePickerViewModel
import com.letstwinkle.freebee.util.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respondOk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import kotlin.test.*

private val dec30_2024 = Instant.fromEpochSeconds(1735536626)

class GamePickerTest {
   val navigator = MockPickerNavigator()
   val owner = object : ViewModelStoreOwner {
      override val viewModelStore: ViewModelStore = ViewModelStore()
   }
   
   private val testDispatcher: TestDispatcher = StandardTestDispatcher()
   
   @BeforeTest
   fun setUp() {
      Dispatchers.setMain(testDispatcher)
   }
   
   @AfterTest
   fun tearDown() {
      Dispatchers.resetMain()
   }
   
   private fun ComposeUiTest.setContent() {
      val repository = GamePickerTestRepository()
      val mockEngineConfig = MockEngineConfig().also {
         it.addHandler { request -> respondOk() }
      }
      val mockHttpClienPtovider = object : HttpClientProvider {
         override fun provide(): HttpClient = HttpClient(BetterMockEngine(testDispatcher, mockEngineConfig))
      }
      val viewModel = GamePickerViewModel(repository, mockHttpClienPtovider, MockClock(dec30_2024))
      setContent {
         CompositionLocalProvider(LocalViewModelStoreOwner provides owner) {
            GamePicker(TestRepository(), viewModel = viewModel, pickerNavigator = navigator)
         }
      }
   }
   
   @Test fun testDatePicker() = runComposeUiTest { 
      setContent()
      
      onNodeWithTag("openpicker").performClick()
      waitForIdle()
      
      onNodeWithText("Sunday, December 1, 2024").assertIsNotEnabled()
      onNodeWithText("Monday, December 2, 2024").assertIsEnabled()
      onNodeWithText("Tuesday, December 3, 2024").assertIsNotEnabled()
      onNodeWithText("Wednesday, December 4, 2024").assertIsEnabled()
      
      onNodeWithContentDescription("Change to next month").performClick()
      
      onNodeWithText("Thursday, January 2, 2025").assertIsNotEnabled()
      
      onNodeWithContentDescription("Change to previous month").performClick()
      onNodeWithText("Wednesday, December 4, 2024").performClick()
      onNodeWithText("OK").performClick()
      
      onNodeWithTag("openpicker").assertTextEquals("Dec 4, 2024")
   }
   
   @Test fun testGo() = runComposeUiTest { 
      setContent()
      
      onNodeWithText("Go!").performClick()
      
      assertEquals(1, navigator.openGameLoaderCount)
      assertEquals(dec30_2024.toLocalDateTime(TimeZone.UTC).date, navigator.lastGameDate)
   }
}
