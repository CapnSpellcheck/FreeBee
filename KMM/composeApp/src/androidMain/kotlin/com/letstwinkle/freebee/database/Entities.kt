package com.letstwinkle.freebee.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.letstwinkle.freebee.model.IEnteredWord
import com.letstwinkle.freebee.model.IGame
import com.letstwinkle.freebee.model.IGameProgress
import kotlinx.datetime.Instant

@Entity(indices=[Index("date", unique=true, orders=[Index.Order.DESC])]) 
data class Game(
   @PrimaryKey(autoGenerate = true) val id: Int,
   override val date: Instant,
   override val allowedWords: Set<String>,
   override val centerLetterCode: Int,
   override val otherLetters: String,
   override val geniusScore: Short,
   override val maximumScore: Short,
   @Embedded override val progress: GameProgress
) : IGame {
   override fun equals(other: Any?): Boolean {
      if (javaClass != other?.javaClass) return false
      
      other as Game
      
      if (date != other.date) return false
      if (allowedWords != other.allowedWords) return false
      if (centerLetterCode != other.centerLetterCode) return false
      if (otherLetters != other.otherLetters) return false
      if (geniusScore != other.geniusScore) return false
      if (maximumScore != other.maximumScore) return false
      if (progress != other.progress) return false
      
      return true
   }
   
   override fun hashCode(): Int {
      var result = date.hashCode()
      result = 31*result + allowedWords.hashCode()
      result = 31*result + centerLetterCode
      result = 31*result + otherLetters.hashCode()
      result = 31*result + geniusScore
      result = 31*result + maximumScore
      result = 31*result + progress.hashCode()
      return result
   }
}

data class GameProgress(
   @Ignore override var currentWord: String = "",
   override var score: Short = 0,
) : IGameProgress {
   // TODO
   override val enteredWords: LinkedHashSet<IEnteredWord>
      get() = linkedSetOf()
}