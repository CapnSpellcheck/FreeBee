@file:OptIn(ExperimentalForeignApi::class)

package com.letstwinkle.freebee.database

import com.letstwinkle.freebee.database.swift.CDEnteredWord
import com.letstwinkle.freebee.database.swift.CDGame
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import platform.CoreData.NSManagedObjectID
import platform.Foundation.*

actual typealias EntityIdentifier = NSManagedObjectID

actual class Game(val cdGame: CDGame) {
   actual val date: Instant
      get() = cdGame.date()!!.toKotlinInstant()
   actual val allowedWords: Set<String>
      get() = cdGame.allowedWords() as Set<String>
   actual val centerLetterCode: Int
      get() = cdGame.centerLetterCode()
   actual val otherLetters: String
      get() = cdGame.otherLetters()
   actual val geniusScore: Short
      get() = cdGame.geniusScore()
   actual val maximumScore: Short
      get() = cdGame.maximumScore()
   actual var currentWord: String
      get() = cdGame.progress().currentWord()
      set(value) {
         cdGame.progress().setCurrentWord(value)
      }
   actual var score: Short
      get() = cdGame.progress().score()
      set(value) {
         cdGame.progress().setScore(value)
      }
   actual val uniqueID: EntityIdentifier
      get() = cdGame.objectID()
   
   fun withWords(): GameWithWords = GameWithWords(this)
}

actual class GameWithWords(actual val game: Game) {
   actual val enteredWords: Set<EnteredWord>
      get() {
         val nsOrderedSet = game.cdGame.progress().mutableOrderedSetValueForKey("enteredWords")
         val linkedSet = LinkedHashSet<EnteredWord>(initialCapacity = nsOrderedSet.count.toInt())
         nsOrderedSet.enumerateObjectsUsingBlock { enteredWord, _, _ ->
            linkedSet.add(EnteredWord(enteredWord as CDEnteredWord))
         }
         return linkedSet
      }
}

actual class EnteredWord(private val cdWord: CDEnteredWord) {
   actual val value: String
      get() = cdWord.value()
}
