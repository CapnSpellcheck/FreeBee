@file:OptIn(ExperimentalForeignApi::class)

package com.letstwinkle.freebee.database

import com.letstwinkle.freebee.database.swift.CDEnteredWord
import com.letstwinkle.freebee.database.swift.CDGame
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.datetime.*
import platform.CoreData.NSManagedObjectID
import platform.Foundation.*

class Game(val cdGame: CDGame) : IGame<NSManagedObjectID> {
   override val date: LocalDate
      get() = cdGame.date()!!.toKotlinInstant()
         .toLocalDateTime(TimeZone.UTC)
         .date
   override val allowedWords: Set<String>
      get() = cdGame.allowedWords() as Set<String>
   override val centerLetterCode: Int
      get() = cdGame.centerLetterCode()
   override var otherLetters: String
      get() = cdGame.otherLetters()
      set(value) {
         cdGame.setOtherLetters(value)
      }
   override val geniusScore: Short
      get() = cdGame.geniusScore()
   override val maximumScore: Short
      get() = cdGame.maximumScore()
   override var currentWord: String
      get() = cdGame.progress().currentWord()
      set(value) {
         cdGame.progress().setCurrentWord(value)
      }
   override var score: Short
      get() = cdGame.progress().score()
      set(value) {
         cdGame.progress().setScore(value)
      }
   override val uniqueID: NSManagedObjectID
      get() = cdGame.objectID()
   
   fun withWords(): GameWithWords = GameWithWords(this)
}

class GameWithWords(override val game: Game) : IGameWithWords<NSManagedObjectID> {
   override val enteredWords: Set<EnteredWord>
      get() {
         val nsOrderedSet = game.cdGame.progress().mutableOrderedSetValueForKey("enteredWords")
         val linkedSet = LinkedHashSet<EnteredWord>(initialCapacity = nsOrderedSet.count.toInt())
         nsOrderedSet.enumerateObjectsUsingBlock { enteredWord, _, _ ->
            linkedSet.add(EnteredWord(enteredWord as CDEnteredWord))
         }
         return linkedSet
      }
   
   override fun add(word: IEnteredWord) {
      add(word as EnteredWord)
   }
   
   fun add(word: EnteredWord) {
      game.cdGame.progress().addEnteredWordsObject(word.cdWord)
   }
}

class EnteredWord(val cdWord: CDEnteredWord) : IEnteredWord {
   override val value: String
      get() = cdWord.value()
}
