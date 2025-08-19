package com.letstwinkle.freebee.util

import com.letstwinkle.freebee.database.FreeBeeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.*

private typealias ITestRepository = FreeBeeRepository<Int, MockGame, MockGame>

class TestRepository : ITestRepository {
   var games = arrayListOf<MockGame>()
   
   override suspend fun createGame(
      date: LocalDate,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
   ): Int = createGame(date, allowedWords, centerLetterCode, otherLetters, geniusScore, maximumScore, 0)
   
   fun createGame(
      date: LocalDate,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
      currentScore: Short,
      scoredAt: Instant? = null
   ): Int {
      val game = MockGame(date, allowedWords, centerLetterCode, otherLetters, geniusScore, maximumScore, linkedSetOf(), games.size, scoredAt)
      game.score = currentScore
      games.add(game)
      return game.uniqueID
   }
   
   override fun fetchGamesLive(orderByScored: Boolean): Flow<List<MockGame>> =
      if (orderByScored)
         flowOf(games.sortedWith { a, b ->
            a.scoredAt?.toEpochMilliseconds()?.let {
               (b.scoredAt ?: Instant.DISTANT_PAST).toEpochMilliseconds().compareTo(it)
            } ?: b.date.compareTo(a.date)
         })
      else flowOf(games.sortedByDescending { it.date })
   
   override suspend fun fetchGame(date: LocalDate): MockGame? {
      return games.firstOrNull { it.date == date }
   }
   
   override suspend fun getStartedGameCount(): Int = games.count { it.score > 0 }
   
   override suspend fun getGeniusGameCount(): Int =
      games.count { it.score >= it.geniusScore }
   
   override suspend fun getEnteredWordCount(): Int = games.sumOf { it.enteredWords.size }
   
   override suspend fun findGameDateForEnteredWord(word: String): LocalDate? =
      games.firstOrNull { 
         it.enteredWords.contains(MockWord(word))
      }?.date
   
   override suspend fun updateGameScore(game: MockGame, score: Short, scoredAt: Instant) {
      game.game.score = score
      game.game.scoredAt = scoredAt
   }
   
   override suspend fun updateOtherLetters(game: MockGame, otherLetters: String): MockGame {
      game.otherLetters = otherLetters
      return game
   }
   
   override suspend fun addEnteredWord(gameWithWords: MockGame, word: String): Boolean {
      gameWithWords.add(MockWord(value = word))
      return true
   }
   
   override fun hasGameForDate(date: LocalDate): Boolean =
      games.find { it.date == date } != null
   
   override suspend fun executeAndSave(transaction: suspend (ITestRepository) -> Unit): Boolean {
      transaction(this)
      return true
   }
   
   override suspend fun fetchGameWithWords(gameID: Int): MockGame = games[gameID]
}
