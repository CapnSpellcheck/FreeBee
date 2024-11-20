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
   var currentWord: String
   var score: Short
   
   val uniqueID: EntityIdentifier
}

interface IGameWithWords {
   val game: IGame
   val enteredWords: Set<IEnteredWord>
}

interface IEnteredWord {
   val value: String
}

inline val IGame.currentWordDisplay: String
   get() = currentWord + "_"

inline val IGame.centerLetterCharacter: Char
   get() = Char(centerLetterCode)

inline val IGame.isComplete: Boolean
   get() = score >= maximumScore

inline val IGame.isGenius: Boolean
   get() = score >= geniusScore

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
