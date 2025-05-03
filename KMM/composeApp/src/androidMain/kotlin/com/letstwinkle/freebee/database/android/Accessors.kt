package com.letstwinkle.freebee.database.android

import androidx.room.*
import com.letstwinkle.freebee.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface GameDAO {
   @Insert
   suspend fun createGame(game: Game): Long
   
   suspend fun createGame(
      date: LocalDate,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
   ): Long {
      val game = Game(
         0,
         date,
         allowedWords,
         centerLetterCode,
         otherLetters,
         geniusScore,
         maximumScore,
      )
      return createGame(game)
   }
   
   @Query("SELECT * FROM Game ORDER BY date DESC")
   fun fetchGamesLive(): Flow<List<Game>>
   
   @Query("SELECT * FROM Game WHERE id = :gameID")
   suspend fun fetchGame(gameID: Long): Game
   
   @Query("SELECT * FROM Game WHERE date = :date")
   suspend fun fetchGame(date: LocalDate): Game?
   
   @Query("SELECT COUNT(*) FROM Game WHERE score > 0")
   suspend fun getStartedCount(): Int
   
   @Query("SELECT COUNT(*) FROM Game WHERE score >= geniusScore")
   suspend fun getGeniusCount(): Int

   @Query("SELECT EXISTS(SELECT * FROM Game WHERE date = :date)")
   fun hasDate(date: LocalDate): Boolean
   
   @Update(entity = Game::class)
   suspend fun saveGameScore(gameScore: GameScore)
   
   @Update(entity = Game::class)
   suspend fun saveOtherLetters(game: GameOtherLetters)
}

@Dao
interface EnteredWordDAO {
   @Query("SELECT COUNT(*) FROM EnteredWord")
   suspend fun getTotalCount(): Int
   
   @Query("SELECT * FROM EnteredWord WHERE gameId = :gameId ORDER BY id")
   suspend fun getGameWords(gameId: Long): List<EnteredWord>
   
   @Insert
   suspend fun addEnteredWord(word: EnteredWord)
   
   @Query("SELECT date FROM Game LEFT JOIN EnteredWord ew ON Game.id = ew.gameId WHERE value = :word LIMIT 1")
   suspend fun findGameDateForEnteredWord(word: String): LocalDate?
   
}
