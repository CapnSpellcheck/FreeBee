package com.letstwinkle.freebee.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.TypeConverters
import com.letstwinkle.freebee.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

@Database(entities=[Game::class, EnteredWord::class], version=2)
@TypeConverters(RoomConverters::class)
abstract class RoomDatabase : androidx.room.RoomDatabase(), FreeBeeRepository {
   abstract fun gameDAO(): GameDAO
   abstract fun enteredWordDAO(): EnteredWordDAO
   
   override suspend fun createGame(
      date: Instant,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
   ) {
      gameDAO().createGame(date, allowedWords, centerLetterCode, otherLetters, geniusScore, maximumScore)
   }
   
   override fun fetchGamesLive(): Flow<List<IGame>> {
      return gameDAO().fetchGamesLive()
   }
   
   override suspend fun getStartedGameCount(): Int = gameDAO().getStartedCount()
   
   override suspend fun getGeniusGameCount(): Int = gameDAO().getGeniusCount()
   
   override suspend fun getEnteredWordCount(): Int = enteredWordDAO().getTotalCount()
   
   companion object {
      @Volatile private var instance: RoomDatabase? = null
      
      fun getDatabase(context: Context): RoomDatabase {
         return instance ?: synchronized(this) {
            instance = Room.databaseBuilder(context, RoomDatabase::class.java, "game.db")
               .apply {
                  if (BuildConfig.DEBUG) {
                     fallbackToDestructiveMigration()
                  }
               }
               .build()
            this.debugSeed(context)
            instance!!
         }
      }
      
      // Insert a few mock games
      private fun debugSeed(context: Context) {
         if (BuildConfig.DEBUG) {
            val gameDAO = getDatabase(context).gameDAO()
            GlobalScope.launch(Dispatchers.IO) {
               if (gameDAO.getStartedCount() > 0)
                  return@launch
               var game = Game(
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
               )
               gameDAO.createGame(game)
               
               game = Game(
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
               )
               gameDAO.createGame(game)
               
               game = Game(
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
               gameDAO.createGame(game)
            }
         }
      }
   }
}
