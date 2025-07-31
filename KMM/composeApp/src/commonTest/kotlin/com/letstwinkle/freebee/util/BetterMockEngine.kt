package com.letstwinkle.freebee.util

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.CoroutineDispatcher
import kotlin.coroutines.CoroutineContext

class BetterMockEngine(
   override val dispatcher: CoroutineDispatcher, // 👈 Notice this
   private val mockEngineConfig: MockEngineConfig,
) : HttpClientEngine {
   
   private val mockEngine: MockEngine = MockEngine(mockEngineConfig)
   
   override val coroutineContext: CoroutineContext
      get() = mockEngine.coroutineContext + dispatcher // 👈 And notice this
   
   override fun close() {
      mockEngine.close()
   }
   
   @InternalAPI
   override suspend fun execute(data: HttpRequestData): HttpResponseData =
      mockEngine.execute(data)
   
   override val config: HttpClientEngineConfig
      get() = mockEngineConfig
   // override other stuff from HttpClientEngine and delegate to mockEngine.<method name>
}
