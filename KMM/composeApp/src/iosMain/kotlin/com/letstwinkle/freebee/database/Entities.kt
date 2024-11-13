@file:Suppress("UNCHECKED_CAST")

package com.letstwinkle.freebee.database

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toNSDate
import platform.CoreData.NSEntityDescription
import platform.CoreData.NSManagedObject
import platform.CoreData.NSManagedObjectContext
import platform.Foundation.NSDate
import platform.Foundation.NSOrderedSet
import platform.Foundation.enumerateObjectsUsingBlock
import platform.Foundation.mutableOrderedSetValueForKey
import platform.darwin.NSUInteger

class Game : IGame {
   constructor(managedObject: NSManagedObject) {
      this.managedObject = managedObject
   }
   
   constructor(context: NSManagedObjectContext) {
      managedObject = NSManagedObject(
         entity = NSEntityDescription.entityForName("Game", context)!!,
         insertIntoManagedObjectContext = context
      )
   }
   
   private val managedObject: NSManagedObject
   
   override var allowedWords: Set<String>
      get() = managedObject.valueForKey(Keys.allowedWords) as Set<String>
      set(value) {
         managedObject.setValue(value, forKey = Keys.allowedWords)
      }
   
   override var centerLetterCode: Int
      get() = managedObject.valueForKey(Keys.centerLetterCode) as Int
      set(value) {
         managedObject.setValue(value, forKey = Keys.centerLetterCode)
      }
   
   override var date: Instant
      get() = (managedObject.valueForKey(Keys.date) as NSDate).toKotlinInstant()
      set(value) {
         managedObject.setValue(value.toNSDate(), forKey = Keys.date)
      }
   
   override var geniusScore: Short
      get() = managedObject.valueForKey(Keys.geniusScore) as Short
      set(value) {
         managedObject.setValue(value, forKey = Keys.geniusScore)
      }
   
   override var maximumScore: Short
      get() = managedObject.valueForKey(Keys.maximumScore) as Short
      set(value) {
         managedObject.setValue(value, forKey = Keys.maximumScore)
      }
   
   override var otherLetters: String
      get() = managedObject.valueForKey(Keys.otherLetters) as String
      set(value) {
         managedObject.setValue(value, forKey = Keys.otherLetters)
      }
   
   override var progress: GameProgress
      get() = managedObject.valueForKey(Keys.progress) as GameProgress
      set(value) {
         managedObject.setValue(value, forKey = Keys.progress)
      }
   override val uniqueID: Any
      get() = managedObject.objectID.URIRepresentation()
   
   private object Keys {
      const val allowedWords = "allowedWords"
      const val centerLetterCode = "centerLetterCode"
      const val date = "date"
      const val geniusScore = "geniusScore"
      const val maximumScore = "maximumScore"
      const val otherLetters = "otherLetters"
      const val progress = "progress"
   }
}

class GameProgress(context: NSManagedObjectContext) : IGameProgress {
   private val managedObject = NSManagedObject(
      entity = NSEntityDescription.entityForName("GameProgress", context)!!,
      insertIntoManagedObjectContext = context
   )
   
   override var currentWord = ""
   override var score: Short
      get() = managedObject.valueForKey(Keys.score) as Short
      set(value) {
         managedObject.setValue(value, forKey = Keys.score)
      }
   
   // It's stored as an NSOrderedSet.
   @OptIn(ExperimentalForeignApi::class)
   override val enteredWords: LinkedHashSet<IEnteredWord>
      get() {
         val nsOrderedSet = managedObject.valueForKey(Keys.enteredWords) as NSOrderedSet
         val linkedSet = LinkedHashSet<IEnteredWord>(initialCapacity = nsOrderedSet.count.toInt())
         nsOrderedSet.enumerateObjectsUsingBlock { enteredWord, _, _ ->
            linkedSet.add(enteredWord as IEnteredWord)
         }
         return linkedSet
      }
   
   @OptIn(ExperimentalForeignApi::class)
   fun addEnteredWord(enteredWord: IEnteredWord) {
      val enteredWordSet = managedObject.mutableOrderedSetValueForKey(Keys.enteredWords)
      enteredWordSet.insertObject(enteredWord, atIndex = 0.convert<NSUInteger>())
   }
   
   private object Keys {
      const val score = "score"
      const val enteredWords = "enteredWords"
   }
}