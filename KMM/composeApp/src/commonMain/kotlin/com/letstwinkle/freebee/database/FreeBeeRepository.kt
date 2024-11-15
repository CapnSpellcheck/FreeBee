package com.letstwinkle.freebee.database

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface FreeBeeRepository {
   suspend fun createGame(
      date: Instant,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
   )
   
   fun fetchGamesLive(): Flow<List<IGame>>
   
   suspend fun getStartedGameCount(): Int
   
   suspend fun getGeniusGameCount(): Int
   
   suspend fun getEnteredWordCount(): Int
}
