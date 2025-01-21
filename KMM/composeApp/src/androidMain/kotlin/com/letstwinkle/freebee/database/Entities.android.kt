package com.letstwinkle.freebee.database

import android.os.Parcelable
import androidx.room.*
import com.letstwinkle.freebee.LocalDateClassParceler
import kotlinx.datetime.LocalDate
import kotlinx.parcelize.*

actual typealias EntityIdentifier = Long

@Entity(indices = [Index("date", unique=true, orders=[Index.Order.DESC])])
@Parcelize
@TypeParceler<LocalDate, LocalDateClassParceler>
actual data class Game(
   @PrimaryKey(autoGenerate = true) val id: EntityIdentifier,
   actual val date: LocalDate,
   actual val allowedWords: Set<String>,
   actual val centerLetterCode: Int,
   actual val otherLetters: String,
   actual val geniusScore: Short,
   actual val maximumScore: Short,
   actual var score: Short = 0,
) : Parcelable {
   @Ignore @IgnoredOnParcel
   actual var currentWord: String = currentWordStore.getOrDefault(id, "")
      set(value) {
         field = value
         currentWordStore[id] = value
      }
   
   actual val uniqueID: EntityIdentifier
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
      val currentWordStore = mutableMapOf<EntityIdentifier, String>()
   }
}

actual data class GameWithWords(
   actual val game: Game,
   val enteredWordsHash: LinkedHashSet<EnteredWord>
) {
   actual val enteredWords: Set<EnteredWord>
      get() = enteredWordsHash
}

@Entity(
   foreignKeys = [ForeignKey(Game::class, arrayOf("id"), arrayOf("gameId"), ForeignKey.RESTRICT)],
   indices = [Index("gameId", "value", unique=true)]
)
actual data class EnteredWord(
   @PrimaryKey(autoGenerate = true) val id: EntityIdentifier = 0,
   @ColumnInfo(index = true) val gameId: EntityIdentifier,
   actual val value: String,
)

data class GameScore(
   @ColumnInfo("id") val gameId: EntityIdentifier,
   val score: Short
)
