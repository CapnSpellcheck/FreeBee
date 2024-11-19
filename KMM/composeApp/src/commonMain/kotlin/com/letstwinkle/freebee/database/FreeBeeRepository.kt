package com.letstwinkle.freebee.database

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface FreeBeeRepository<Game: IGame, GameWithWords: IGameWithWords> {
   suspend fun createGame(
      date: Instant,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
   )
   
   fun fetchGamesLive(): Flow<List<Game>>
   
   suspend fun fetchGameWithWords(gameID: EntityIdentifier): GameWithWords
   
   suspend fun getStartedGameCount(): Int
   
   suspend fun getGeniusGameCount(): Int
   
   suspend fun getEnteredWordCount(): Int
   
   suspend fun addEnteredWord(gameWithWords: GameWithWords, word: String): Boolean
   
   suspend fun updateGameScore(game: GameWithWords, score: Short)
   
   suspend fun executeAndSave(transaction: suspend (FreeBeeRepository<Game, GameWithWords>) -> Unit): Boolean
}

typealias CovariantFreeBeeRepository = FreeBeeRepository<out IGame, out IGameWithWords>
