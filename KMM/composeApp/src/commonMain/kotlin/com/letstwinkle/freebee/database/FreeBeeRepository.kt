package com.letstwinkle.freebee.database

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface FreeBeeRepository<Id, Game: IGame<Id>, GameWithWords: IGameWithWords<Id>> {
   suspend fun createGame(
      date: LocalDate,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
   ): Id
   
   fun fetchGamesLive(): Flow<List<Game>>
   
   suspend fun fetchGame(date: LocalDate): Game?
   
   suspend fun fetchGameWithWords(gameID: Id): GameWithWords
   
   suspend fun getStartedGameCount(): Int
   
   suspend fun getGeniusGameCount(): Int
   
   suspend fun getEnteredWordCount(): Int
   
   suspend fun addEnteredWord(gameWithWords: GameWithWords, word: String): Boolean
   
   suspend fun updateGameScore(game: GameWithWords, score: Short)
   
   // Not 'suspend' so can be used for synchronous SelectedDates
   fun hasGameForDate(date: LocalDate): Boolean
   
   suspend fun executeAndSave(
      transaction: suspend (FreeBeeRepository<Id, Game, GameWithWords>) -> Unit
   ): Boolean
}

typealias AnyFreeBeeRepository = FreeBeeRepository<*, *, *>
