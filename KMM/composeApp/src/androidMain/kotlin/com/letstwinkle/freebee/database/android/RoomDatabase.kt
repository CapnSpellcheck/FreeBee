package com.letstwinkle.freebee.database.android

import android.content.Context
import androidx.room.*
import com.letstwinkle.freebee.BuildConfig
import com.letstwinkle.freebee.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Database(entities = [Game::class, EnteredWord::class], version = 6)
@TypeConverters(RoomConverters::class)
abstract class RoomDatabase : androidx.room.RoomDatabase(), FreeBeeRepository {
   abstract fun gameDAO(): GameDAO
   abstract fun enteredWordDAO(): EnteredWordDAO
   
   override suspend fun createGame(
      date: LocalDate,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short
   ): EntityIdentifier =
      gameDAO().createGame(date, allowedWords, centerLetterCode, otherLetters, geniusScore, maximumScore)
   
   
   override fun fetchGamesLive(): Flow<List<Game>> {
      return gameDAO().fetchGamesLive()
   }
   
   override suspend fun fetchGameWithWords(gameID: EntityIdentifier): GameWithWords {
      return withTransaction {
         val game = gameDAO().fetchGame(gameID)
         val enteredWordsList = enteredWordDAO().getGameWords(game.id)
         GameWithWords(game, LinkedHashSet(enteredWordsList))
      }
   }
   
   override suspend fun getStartedGameCount(): Int = gameDAO().getStartedCount()
   
   override suspend fun getGeniusGameCount(): Int = gameDAO().getGeniusCount()
   
   override suspend fun getEnteredWordCount(): Int = enteredWordDAO().getTotalCount()
   
   override suspend fun addEnteredWord(gameWithWords: GameWithWords, word: String): Boolean {
      val enteredWord = EnteredWord(gameId = gameWithWords.game.id, value = word)
      enteredWordDAO().addEnteredWord(enteredWord)
      gameWithWords.enteredWordsHash.add(enteredWord)
      return true
   }
   
   override suspend fun updateGameScore(game: GameWithWords, score: Short) {
      gameDAO().saveGameScore(GameScore(game.game.id, score))
      game.game.score = score
   }
   
   override fun hasGameForDate(date: LocalDate): Boolean = gameDAO().hasDate(date)
   
   // TODO: to match iOS, model revert should be implemented on failure, somehow…
   override suspend fun
      executeAndSave(transaction: suspend (FreeBeeRepository) -> Unit): Boolean
   {
      try {
         withTransaction {
            transaction(this)
         }
      } catch (e: Exception) {
         return false
      }
      return true
   }
   
   companion object {
      @Volatile private var instance: RoomDatabase? = null
      
      fun getDatabase(context: Context): RoomDatabase {
         return instance ?: synchronized(this) {
            instance = Room.databaseBuilder(context, RoomDatabase::class.java, "game.db")
               // allowed for GamePickerViewModel#selectableDates ONLY!
               .allowMainThreadQueries()
               .apply {
                  if (BuildConfig.DEBUG) {
                     fallbackToDestructiveMigration()
                  }
               }
               .build()
            debugSeed(context)
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
               )
               gameDAO.createGame(game)
               
               game = Game(
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
               )
               gameDAO.createGame(game)
               
               game = Game(
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
                  geniusScore = 186,
                  maximumScore = 266,
                  score = 0
               )
               gameDAO.createGame(game)
            }
         }
      }
   }
}
