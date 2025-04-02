@file:OptIn(ExperimentalCoroutinesApi::class)

package com.letstwinkle.freebee.unit

import com.letstwinkle.freebee.HttpClientProvider
import com.letstwinkle.freebee.screens.loader.*
import com.letstwinkle.freebee.util.BetterMockEngine
import com.letstwinkle.freebee.util.TestRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDate
import org.lighthousegames.logging.KmLogging
import org.lighthousegames.logging.LogLevel
import kotlin.test.*

class GameLoaderViewModelTest {
   val mockClient: HttpClient
   val mockClientProvider: HttpClientProvider
   val testDispatcher: TestDispatcher = StandardTestDispatcher()
   
   lateinit var statusCode: HttpStatusCode
   lateinit var viewModel: GameLoaderViewModel<*>
   lateinit var repository: TestRepository
   
   init {
      KmLogging.setLogLevel(LogLevel.Off)
      val mockEngineConfig = MockEngineConfig()
      mockEngineConfig.addHandler { request ->
         respondError(statusCode)
      }
      mockClient = HttpClient(BetterMockEngine(testDispatcher, mockEngineConfig))
      mockClientProvider = object : HttpClientProvider {
         override fun provide(): HttpClient = mockClient
      }
   }
   
   @BeforeTest
   fun setUp() {
      Dispatchers.setMain(testDispatcher)
      repository = TestRepository()
      statusCode = HttpStatusCode.OK
      val mockClientProvider = object : HttpClientProvider {
         override fun provide(): HttpClient = mockClient
      }
      viewModel = GameLoaderViewModel(
         LocalDate.fromEpochDays(0),
         httpClientProvider = mockClientProvider,
         repository = repository,
         parsingContext = testDispatcher,
         parser = object : GameLoaderViewModel.GameHTMLParser {
            override suspend fun parse(
               date: LocalDate,
               html: String,
               onCenterLetterNotUnique: (suspend (List<Char>) -> Char)?
            ) = GameData(LocalDate.fromEpochDays(0), emptySet(), ' '.code, "", 0, 0)
         }
      )
   }
   
   @AfterTest
   fun tearDown() {
      Dispatchers.resetMain()
   }
   
   @Test fun testStatus_Loading() = runTest(testDispatcher) {
      assertIs<LoadingStatus.Loading>(viewModel.status.value, "initial status is Loading")
   }
   
   @Test fun testStatus_DownloadError() = runTest(testDispatcher) {
      statusCode = HttpStatusCode.NotFound
      viewModel.load { it.first() }
      advanceUntilIdle()
      
      assertIs<LoadingStatus.Error>(viewModel.status.value, "HTTP request failed, status is Error")
   }
   
   @Test fun testStatus_Finished() = runTest(testDispatcher) {
      viewModel.load { it.first() }
      advanceUntilIdle()
      
      assertIs<LoadingStatus.Finished<*>>(viewModel.status.value, "HTTP request succeeded, status is Finished")
   }
}