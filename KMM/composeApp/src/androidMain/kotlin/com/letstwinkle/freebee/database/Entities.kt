package com.letstwinkle.freebee.database

import androidx.room.*
import kotlinx.datetime.Clock
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
   @Embedded(prefix = "progress_") override val progress: GameProgress
) : IGame {
   override val uniqueID: Any
      get() = id
   
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

@Entity(foreignKeys=[ForeignKey(Game::class, arrayOf("id"), arrayOf("gameId"))])
data class EnteredWord(
   @PrimaryKey(autoGenerate = true) val id: Int,
   val gameId: Int,
   override val value: String,
   val timestamp: Instant = Clock.System.now(),
) : IEnteredWord
