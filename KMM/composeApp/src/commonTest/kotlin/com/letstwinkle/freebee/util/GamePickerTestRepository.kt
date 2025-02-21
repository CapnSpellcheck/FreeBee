package com.letstwinkle.freebee.util

import com.letstwinkle.freebee.database.FreeBeeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlin.random.Random

class GamePickerTestRepository : FreeBeeRepository<Int, MockGame, MockGame> {
   override suspend fun createGame(
      date: LocalDate,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
   ): Int = Random.nextInt()
   
   override fun fetchGamesLive(): Flow<List<MockGame>> = flowOf()
   
   override suspend fun fetchGame(date: LocalDate): MockGame? = null
   
   override suspend fun getStartedGameCount(): Int = 0
   
   override suspend fun getGeniusGameCount(): Int = 0
   
   override suspend fun getEnteredWordCount(): Int = 0
   
   override fun hasGameForDate(date: LocalDate): Boolean = date.dayOfMonth % 2 == 1
   
   override suspend fun executeAndSave(transaction: suspend (FreeBeeRepository<Int, MockGame, MockGame>) -> Unit): Boolean = false
   
   override suspend fun updateGameScore(game: MockGame, score: Short) {
      throw NotImplementedError()
   }
   
   override suspend fun addEnteredWord(gameWithWords: MockGame, word: String): Boolean = false
   
   override suspend fun fetchGameWithWords(gameID: Int): MockGame {
      throw NotImplementedError()
   }
}
