package com.letstwinkle.freebee.database.android

import androidx.room.*
import com.letstwinkle.freebee.database.EntityIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

@Dao
interface GameDAO {
   @Insert
   suspend fun createGame(game: Game)
   
   suspend fun createGame(
      date: Instant,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
   ) {
      val game = Game(
         0,
         date,
         allowedWords,
         centerLetterCode,
         otherLetters,
         geniusScore,
         maximumScore,
      )
      createGame(game)
   }
   
   @Query("SELECT * FROM Game ORDER BY date DESC")
   fun fetchGamesLive(): Flow<List<Game>>
   
   @Query("SELECT * FROM Game WHERE id = :gameID")
   suspend fun fetchGame(gameID: EntityIdentifier): Game
   
   @Query("SELECT COUNT(*) FROM Game WHERE score > 0")
   suspend fun getStartedCount(): Int
   
   @Query("SELECT COUNT(*) FROM Game WHERE score >= geniusScore")
   suspend fun getGeniusCount(): Int

   @Update(entity = Game::class)
   suspend fun saveGameScore(gameScore: GameScore)
}

@Dao
interface EnteredWordDAO {
   @Query("SELECT COUNT(*) FROM EnteredWord")
   suspend fun getTotalCount(): Int
   
   @Query("SELECT * FROM EnteredWord WHERE gameId = :gameId ORDER BY id")
   suspend fun getGameWords(gameId: Int): List<EnteredWord>
   
   @Insert
   suspend fun addEnteredWord(word: EnteredWord)
   
}