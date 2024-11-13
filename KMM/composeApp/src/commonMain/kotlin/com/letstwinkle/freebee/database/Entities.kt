package com.letstwinkle.freebee.database

import kotlinx.datetime.Instant

interface IGame {
   val date: Instant
   val allowedWords: Set<String>
   val centerLetterCode: Int
   val otherLetters: String
   val geniusScore: Short
   val maximumScore: Short
   val progress: IGameProgress
   val uniqueID: Any
}

interface IGameProgress {
   var currentWord: String
   var score: Short
   val enteredWords: Set<IEnteredWord>
}

interface IEnteredWord {
   val value: String
}

fun IGameProgress.hasEntered(word: String): Boolean =
   this.enteredWords.any { it.value == word }

val IGameProgress.currentWordDisplay: String
   get() = currentWord + "_"

val IGame.isComplete: Boolean
   get() = progress.score >= maximumScore