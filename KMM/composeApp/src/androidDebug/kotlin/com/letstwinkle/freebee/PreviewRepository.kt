package com.letstwinkle.freebee

import com.letstwinkle.freebee.database.*
import com.letstwinkle.freebee.database.android.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

class PreviewRepository : FreeBeeRepository {
   private var games = mutableListOf(
      Game(
         id = 1,
         date = LocalDate(2010, 1, 1),
         allowedWords = setOf(
            "accept",
            "acetate",
            "affect",
            "cafe",
            "cape",
            "effect",
            "face",
            "facet",
            "fate",
            "feet",
            "pace"
         ),
         centerLetterCode = 'e'.code,
         otherLetters = "yatpcf",
         geniusScore = 89,
         maximumScore = 127,
         score = 123
      ),
      Game(
         id = 2,
         date = LocalDate(2011, 1, 1),
         allowedWords = setOf(
            "accrual",
            "accuracy",
            "actual",
            "actually",
            "actuary",
            "aura",
            "aural",
            "cull",
            "cult",
            "cultural",
            "culturally",
            "curl",
            "curly",
            "curry",
            "curt",
            "lull",
            "rural",
            "rutty",
            "tactual",
            "taut",
            "truly",
            "tutu",
            "yucca"
         ),
         centerLetterCode = 'u'.code,
         otherLetters = "rlcayt",
         geniusScore = 113,
         maximumScore = 161,
         score = 55
      ),
      Game(
         id = 3,
         date = LocalDate(2012, 1, 1),
         allowedWords = setOf(
            "acacia",
            "arch",
            "archaic",
            "arctic",
            "attach",
            "attic",
            "attract",
            "carat",
            "cart",
            "cataract",
            "catch",
            "cathartic",
            "chair",
            "charm",
            "chart",
            "chat",
            "chia",
            "chit",
            "chitchat",
            "citric",
            "cram",
            "critic",
            "hatch",
            "itch",
            "march",
            "match",
            "mimic",
            "rich",
            "tactic",
            "tract"
         ),
         centerLetterCode = 'c'.code,
         otherLetters = "mihatr",
         geniusScore = 9,
         maximumScore = 266,
      )
   )
   
   private var enteredWordsMap = mapOf(
      1 to listOf("cafe", "facet", "face").map { EnteredWord(0, 1, it) }.toTypedArray(),
      2 to listOf("taut", "curt", "rural").map { EnteredWord(0, 2, it) }.toTypedArray(),
      3 to listOf("charm", "attach", "itch", "match", "cram").map { EnteredWord(0, 3, it) }.toTypedArray(),
   )
   
   override suspend fun createGame(
      date: LocalDate,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
   ) {
      games.add(
         Game(
            id = games.size + 1,
            date = date,
            allowedWords = allowedWords,
            centerLetterCode = centerLetterCode,
            otherLetters = otherLetters,
            geniusScore = geniusScore,
            maximumScore = maximumScore,
         )
      )
   }
   
   override fun fetchGamesLive(): Flow<List<Game>> {
      return flowOf(games.sortedByDescending { it.date })
   }
   
   override suspend fun fetchGameWithWords(gameID: Int): GameWithWords {
      val game = games.first { it.id == gameID }
      return GameWithWords(game, linkedSetOf(*(enteredWordsMap[gameID] ?: arrayOf())))
   }
   
   override suspend fun getStartedGameCount(): Int = 255
   override suspend fun getGeniusGameCount(): Int = 188
   
   override suspend fun getEnteredWordCount(): Int = 999
   override suspend fun executeAndSave(transaction: suspend (FreeBeeRepository) -> Unit): Boolean {
      transaction(this)
      return true
   }
   
   override suspend fun updateGameScore(game: GameWithWords, score: Short) {
      game.game.score = score
   }
   
   override fun hasGameForDate(date: LocalDate): Boolean = games.find { it.date == date } != null
   
   override suspend fun addEnteredWord(gameWithWords: GameWithWords, word: String): Boolean {
      gameWithWords.enteredWordsHash.add(EnteredWord(gameId = gameWithWords.game.id, value = word))
      return true
   }
   
}
