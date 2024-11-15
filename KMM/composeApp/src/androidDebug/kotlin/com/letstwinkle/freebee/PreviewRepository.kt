package com.letstwinkle.freebee

import com.letstwinkle.freebee.database.FreeBeeRepository
import com.letstwinkle.freebee.database.Game
import com.letstwinkle.freebee.database.GameProgress
import com.letstwinkle.freebee.database.IGame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Instant

class PreviewRepository : FreeBeeRepository {
   private var games = mutableListOf(
      Game(
         id = 1,
         date = Instant.fromEpochSeconds(1725840000),
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
         progress = GameProgress(score = 123)
      ),
      Game(
         id = 2,
         date = Instant.fromEpochSeconds(1540166400),
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
         progress = GameProgress(score = 55)
      ),
      Game(
         id = 3,
         date = Instant.fromEpochSeconds(1614902400),
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
         geniusScore = 186,
         maximumScore = 266,
         progress = GameProgress()
      )
   )
   
   override suspend fun createGame(
      date: Instant,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
   ) {
      games.add(Game(
         id = games.size + 1,
         date = date,
         allowedWords = allowedWords,
         centerLetterCode = centerLetterCode,
         otherLetters = otherLetters,
         geniusScore = geniusScore,
         maximumScore = maximumScore,
         progress = GameProgress()
      ))
   }
   
   override fun fetchGamesLive(): Flow<List<IGame>> {
      return flowOf(games.sortedByDescending { it.date })
   }
   
   override suspend fun getStartedGameCount(): Int = 255
   override suspend fun getGeniusGameCount(): Int = 188
   
   override suspend fun getEnteredWordCount(): Int = 999
   
}
