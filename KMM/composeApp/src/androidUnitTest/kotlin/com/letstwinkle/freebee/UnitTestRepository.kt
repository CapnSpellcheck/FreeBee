package com.letstwinkle.freebee

import com.letstwinkle.freebee.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlin.random.Random

class UnitTestRepository : FreeBeeRepository {
   var games: MutableMap<EntityIdentifier, GameWithWords> = mutableMapOf()
   
   override suspend fun createGame(
      date: LocalDate,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
   ): EntityIdentifier {
      val game = Game(Random.nextLong(), date, allowedWords, centerLetterCode, otherLetters, geniusScore, maximumScore)
      games[game.uniqueID] = GameWithWords(game, linkedSetOf())
      return game.uniqueID
   }
   
   override fun fetchGamesLive(): Flow<List<Game>> =
      flowOf(games.values.map { it.game }.sortedByDescending { it.date })
   
   override suspend fun getStartedGameCount(): Int = games.values.count { it.game.score > 0 }
   
   override suspend fun getGeniusGameCount(): Int = 
      games.values.count { it.game.score >= it.game.geniusScore }
   
   override suspend fun getEnteredWordCount(): Int = games.values.sumOf { it.enteredWords.size }
   
   override suspend fun addEnteredWord(gameWithWords: GameWithWords, word: String): Boolean {
      gameWithWords.enteredWordsHash.add(EnteredWord(gameId = gameWithWords.game.id, value = word))
      return true
   }
   
   override suspend fun updateGameScore(game: GameWithWords, score: Short) {
      game.game.score = score
   }
   
   override fun hasGameForDate(date: LocalDate): Boolean =
      games.values.find { it.game.date == date } != null
   
   override suspend fun executeAndSave(transaction: suspend (FreeBeeRepository) -> Unit): Boolean {
      transaction(this)
      return true
   }
   
   override suspend fun fetchGameWithWords(gameID: EntityIdentifier): GameWithWords = games[gameID]!!
}
