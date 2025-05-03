package com.letstwinkle.freebee.database.android

import android.content.Context
import androidx.room.*
import com.letstwinkle.freebee.AndroidRepository
import com.letstwinkle.freebee.BuildConfig
import com.letstwinkle.freebee.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

interface DefaultAndroidRepository : AndroidRepository {
   override suspend fun createGame(
      date: LocalDate,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
   ): Long { throw NotImplementedError() }
   override fun fetchGamesLive(): Flow<List<Game>> { throw NotImplementedError() }
   override suspend fun fetchGame(date: LocalDate): Game? { throw NotImplementedError() }
   override suspend fun fetchGameWithWords(gameID: Long): GameWithWords { throw NotImplementedError() }
   override suspend fun getStartedGameCount(): Int { throw NotImplementedError() }
   override suspend fun getGeniusGameCount(): Int { throw NotImplementedError() }
   override suspend fun getEnteredWordCount(): Int { throw NotImplementedError() }
   override suspend fun addEnteredWord(gameWithWords: GameWithWords, word: String): Boolean { throw NotImplementedError() }
   override suspend fun findGameDateForEnteredWord(word: String): LocalDate? { throw NotImplementedError() }
   override suspend fun updateGameScore(game: GameWithWords, score: Short) { throw NotImplementedError() }
   override fun hasGameForDate(date: LocalDate): Boolean { throw NotImplementedError() }
   override suspend fun executeAndSave(
      transaction: suspend (FreeBeeRepository<Long, Game, GameWithWords>) -> Unit
   ): Boolean { throw NotImplementedError() }
}

@Database(entities = [Game::class, EnteredWord::class], version = 7)
@TypeConverters(RoomConverters::class)
abstract class RoomDatabase : androidx.room.RoomDatabase(), DefaultAndroidRepository {
   abstract fun gameDAO(): GameDAO
   abstract fun enteredWordDAO(): EnteredWordDAO
   
   override suspend fun createGame(
      date: LocalDate,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short
   ): Long =
      gameDAO().createGame(date, allowedWords, centerLetterCode, otherLetters, geniusScore, maximumScore)
   
   
   override fun fetchGamesLive(): Flow<List<Game>> {
      return gameDAO().fetchGamesLive()
   }
   
   override suspend fun fetchGame(date: LocalDate): Game? = gameDAO().fetchGame(date)
   
   override suspend fun fetchGameWithWords(gameID: Long): GameWithWords {
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
      val enteredWord = EnteredWord(gameId = gameWithWords.game.uniqueID, value = word)
      enteredWordDAO().addEnteredWord(enteredWord)
      gameWithWords.add(enteredWord)
      return true
   }
   
   override suspend fun findGameDateForEnteredWord(word: String): LocalDate? =
      enteredWordDAO().findGameDateForEnteredWord(word)
   
   override suspend fun updateGameScore(game: GameWithWords, score: Short) {
      gameDAO().saveGameScore(GameScore(game.game.uniqueID, score))
      game.game.score = score
   }
   
   override suspend fun updateOtherLetters(game: GameWithWords, otherLetters: String): GameWithWords {
      gameDAO().saveOtherLetters(GameOtherLetters(game.game.uniqueID, otherLetters))
      return game.copy(game = game.game.copy(otherLetters = otherLetters))
   }
   
   override fun hasGameForDate(date: LocalDate): Boolean = gameDAO().hasDate(date)
   
   // TODO: to match iOS, model revert should be implemented on failure, somehow…
   override suspend fun executeAndSave(transaction: suspend (AndroidRepository) -> Unit): Boolean
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
               .addMigrations(MigrationChangingEnteredWordIndexes())
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
               
               game = Game(
                  id = 4,
                  date = LocalDate(2012, 2, 1),
                  allowedWords = setOf(),
                  centerLetterCode = 'x'.code,
                  otherLetters = "qwerty",
                  geniusScore = 9,
                  maximumScore = 9,
                  score = 0
               )
               gameDAO.createGame(game)
               
               game = Game(
                  id = 5,
                  date = LocalDate(2012, 3, 1),
                  allowedWords = setOf(),
                  centerLetterCode = 'x'.code,
                  otherLetters = "qwerty",
                  geniusScore = 9,
                  maximumScore = 9,
                  score = 0
               )
               gameDAO.createGame(game)
               
               game = Game(
                  id = 6,
                  date = LocalDate(2012, 4, 1),
                  allowedWords = setOf(),
                  centerLetterCode = 'x'.code,
                  otherLetters = "qwerty",
                  geniusScore = 9,
                  maximumScore = 9,
                  score = 0
               )
               gameDAO.createGame(game)
               
               val enteredWordDAO = getDatabase(context).enteredWordDAO()
               
               listOf("cart", "taut", "tact", "curl", "nene", "meme").map {
                  enteredWordDAO.addEnteredWord(EnteredWord(gameId = 4, value = it))
               }
               listOf("tact", "curl", "charm", "rich", "tutu").map {
                  enteredWordDAO.addEnteredWord(EnteredWord(gameId = 5, value = it))
               }
               listOf( "charm", "arch", "mama", "papa", "loll").map {
                  enteredWordDAO.addEnteredWord(EnteredWord(gameId = 6, value = it))
               }
            }
         }
      }
   }
}
