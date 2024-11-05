package com.letstwinkle.freebee.database

import com.letstwinkle.freebee.model.IGame
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
   
   suspend fun getGameCount(): Int
}