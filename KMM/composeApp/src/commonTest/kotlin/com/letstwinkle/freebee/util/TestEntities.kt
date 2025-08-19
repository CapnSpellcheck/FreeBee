package com.letstwinkle.freebee.util

import com.letstwinkle.freebee.database.*
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class MockGame(
   override val date: LocalDate,
   override val allowedWords: Set<String>,
   override val centerLetterCode: Int,
   override var otherLetters: String,
   override val geniusScore: Short,
   override val maximumScore: Short,
   override val enteredWords: LinkedHashSet<MockWord>,
   override val uniqueID: Int = 0,
   override var scoredAt: Instant? = null,
) : IGame<Int>, IGameWithWords<Int>
{
   override var currentWord = ""
   override var score: Short = 0
   
   override val game: MockGame
      get() = this
   
   override fun add(word: IEnteredWord) {
      enteredWords.add(MockWord(word.value))
   }
}

data class MockWord(override val value: String) : IEnteredWord
