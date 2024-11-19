package com.letstwinkle.freebee.database

import kotlinx.datetime.Instant

expect class EntityIdentifier

interface IGame {
   val date: Instant
   val allowedWords: Set<String>
   val centerLetterCode: Int
   val otherLetters: String
   val geniusScore: Short
   val maximumScore: Short
//   val progress: IGameProgress
   var currentWord: String
   var score: Short
   
   val uniqueID: EntityIdentifier
}

//interface IGameProgress {
//   var currentWord: String
//   var score: Short
//   val enteredWords: Set<IEnteredWord>
//}

interface IGameWithWords {
   val game: IGame
   val enteredWords: Set<IEnteredWord>
}

interface IEnteredWord {
   val value: String
}

inline val IGame.currentWordDisplay: String
   get() = currentWord + "_"

inline val IGame.isComplete: Boolean
   get() = score >= maximumScore

fun IGame.isPangram(word: String): Boolean {
   if (word.length < 7) 
      return false
   val letters = word.toHashSet()
   val centerChar = Char(centerLetterCode)
   return letters.contains(centerChar) && letters.containsAll(otherLetters.toList())
}

inline fun IGameWithWords.hasEntered(word: String): Boolean =
   enteredWords.any { it.value == word }

inline fun IGameWithWords.isAllowed(word: String): Boolean =
   game.allowedWords.contains(word)
