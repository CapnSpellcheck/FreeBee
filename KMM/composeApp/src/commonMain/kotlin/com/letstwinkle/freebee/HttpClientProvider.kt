package com.letstwinkle.freebee

import io.ktor.client.HttpClient

interface HttpClientProvider {
   fun provide(): HttpClient
}

object DefaultHttpClientProvider : HttpClientProvider {
   override fun provide(): HttpClient = HttpClient()
}
