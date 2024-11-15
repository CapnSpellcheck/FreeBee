package com.letstwinkle.freebee.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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
      val progress = GameProgress()
      val game = Game(
         0,
         date,
         allowedWords,
         centerLetterCode,
         otherLetters,
         geniusScore,
         maximumScore,
         progress
      )
      createGame(game)
   }
   
   @Query("SELECT * FROM Game ORDER BY date DESC")
   fun fetchGamesLive(): Flow<List<Game>>
   
   @Query("SELECT COUNT(*) FROM Game WHERE progress_score > 0")
   suspend fun getStartedCount(): Int
   
   @Query("SELECT COUNT(*) FROM Game WHERE progress_score >= geniusScore")
   suspend fun getGeniusCount(): Int

}

@Dao
interface EnteredWordDAO {
   @Query("SELECT COUNT(*) FROM EnteredWord")
   suspend fun getTotalCount(): Int
}
