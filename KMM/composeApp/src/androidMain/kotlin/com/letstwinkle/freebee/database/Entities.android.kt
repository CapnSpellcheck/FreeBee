package com.letstwinkle.freebee.database

import android.os.Parcelable
import androidx.room.*
import com.letstwinkle.freebee.LocalDateClassParceler
import kotlinx.datetime.LocalDate
import kotlinx.parcelize.*

@Entity(indices = [Index("date", unique=true, orders=[Index.Order.DESC])])
@Parcelize
@TypeParceler<LocalDate, LocalDateClassParceler>
data class Game(
   @PrimaryKey(autoGenerate = true) val id: Long,
   override val date: LocalDate,
   override val allowedWords: Set<String>,
   override val centerLetterCode: Int,
   override val otherLetters: String,
   override val geniusScore: Short,
   override val maximumScore: Short,
   override var score: Short = 0,
) : IGame<Long>, Parcelable {
   @Ignore @IgnoredOnParcel
   override var currentWord: String = currentWordStore.getOrDefault(id, "")
      set(value) {
         field = value
         currentWordStore[id] = value
      }
   
   override val uniqueID: Long
      get() = id
   
   override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false
      
      other as Game
      
      if (date != other.date) return false
      if (allowedWords != other.allowedWords) return false
      if (centerLetterCode != other.centerLetterCode) return false
      if (otherLetters != other.otherLetters) return false
      if (geniusScore != other.geniusScore) return false
      if (maximumScore != other.maximumScore) return false
      if (score != other.score) return false
      
      return true
   }
   
   override fun hashCode(): Int {
      var result = date.hashCode()
      result = 31*result + allowedWords.hashCode()
      result = 31*result + centerLetterCode
      result = 31*result + otherLetters.hashCode()
      result = 31*result + geniusScore
      result = 31*result + maximumScore
      result = 31*result + score
      return result
   }
   
   private companion object {
      /**
       * Room doesn't manage the object graph (i.e. returning the same object instance via similar
       * queries). In order to persist the current word across leaving a game and returning (within
       * the same app execution only), it's stored outside the entity Room returns.
       */
      val currentWordStore = mutableMapOf<Long, String>()
   }
}

data class GameWithWords(
   override val game: Game,
   private val enteredWordsHash: LinkedHashSet<EnteredWord>
) : IGameWithWords<Long> {
   override val enteredWords: Set<EnteredWord>
      get() = enteredWordsHash
   
   override fun add(word: IEnteredWord) {
      add(word as EnteredWord)
   }
   
   fun add(word: EnteredWord) {
      enteredWordsHash.add(word)
   }
}

@Entity(
   foreignKeys = [ForeignKey(Game::class, ["id"], ["gameId"], ForeignKey.RESTRICT)],
   indices = [Index("gameId", "value", unique=true)]
)
data class EnteredWord(
   @PrimaryKey(autoGenerate = true) val id: Long = 0,
   val gameId: Long,
   @ColumnInfo(index = true) override val value: String,
) : IEnteredWord

data class GameScore(
   @ColumnInfo("id") val gameId: Long,
   val score: Short
)
