package com.letstwinkle.freebee.database

import kotlinx.datetime.LocalDate

expect class EntityIdentifier

expect class Game {
   val date: LocalDate
   val allowedWords: Set<String>
   val centerLetterCode: Int
   val otherLetters: String
   val geniusScore: Short
   val maximumScore: Short
   var currentWord: String
   var score: Short
   
   val uniqueID: EntityIdentifier
}

expect class GameWithWords {
   val game: Game
   val enteredWords: Set<EnteredWord>
}

expect class EnteredWord {
   val value: String
}

inline val Game.currentWordDisplay: String
   get() = currentWord.uppercase() + "_"

inline val Game.centerLetterCharacter: Char
   get() = Char(centerLetterCode)

inline val Game.isComplete: Boolean
   get() = score >= maximumScore

inline val Game.isGenius: Boolean
   get() = score >= geniusScore

fun Game.isPangram(word: String): Boolean {
   if (word.length < 7) 
      return false
   val letters = word.toHashSet()
   val centerChar = Char(centerLetterCode)
   return letters.contains(centerChar) && letters.containsAll(otherLetters.toList())
}

inline fun GameWithWords.hasEntered(word: String): Boolean =
   enteredWords.any { it.value == word }

inline fun GameWithWords.isAllowed(word: String): Boolean =
   game.allowedWords.contains(word)
