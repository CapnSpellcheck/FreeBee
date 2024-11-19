@file:Suppress("UNCHECKED_CAST")

package com.letstwinkle.freebee.database

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toNSDate
import platform.CoreData.*
import platform.Foundation.NSDate
import platform.Foundation.NSOrderedSet
import platform.Foundation.enumerateObjectsUsingBlock
import platform.Foundation.mutableOrderedSetValueForKey
import platform.darwin.NSUInteger

actual typealias EntityIdentifier = NSManagedObjectID

class Game : IGame, IGameWithWords {
   constructor(managedObject: NSManagedObject) {
      this.managedObject = managedObject
   }
   
   constructor(context: NSManagedObjectContext) {
      managedObject = NSManagedObject(
         entity = NSEntityDescription.entityForName("Game", context)!!,
         insertIntoManagedObjectContext = context
      )
      progress = GameProgress(context)
   }
   
   private val managedObject: NSManagedObject
   
   override var currentWord = ""
   
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
   
   override var score by progress::score
   
   override var otherLetters: String
      get() = managedObject.valueForKey(Keys.otherLetters) as String
      set(value) {
         managedObject.setValue(value, forKey = Keys.otherLetters)
      }
   
   override val game: IGame
      get() = this
   
   override val enteredWords: Set<IEnteredWord>
      get() = progress.enteredWords
   
   override val uniqueID: EntityIdentifier
      get() = managedObject.objectID
   
   var progress: GameProgress
      get() = managedObject.valueForKey(Keys.progress) as GameProgress
      private set(value) {
         managedObject.setValue(value, forKey = Keys.progress)
      }
   
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

// TODO: this doesn't need to implement an interface (IGameProgress), so convert to NSManagedObject
// inheritance.
class GameProgress(context: NSManagedObjectContext) {
   private val managedObject = NSManagedObject(
      entity = NSEntityDescription.entityForName("GameProgress", context)!!,
      insertIntoManagedObjectContext = context
   )
   
   var score: Short
      get() = managedObject.valueForKey(Keys.score) as Short
      set(value) {
         managedObject.setValue(value, forKey = Keys.score)
      }
   
   // It's stored as an NSOrderedSet. So, this involves a conversion each time accessed. :(
   @OptIn(ExperimentalForeignApi::class)
   val enteredWords: LinkedHashSet<IEnteredWord>
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
      enteredWordSet.insertObject(enteredWord, atIndex = enteredWordSet.count)
   }
   
   private object Keys {
      const val score = "score"
      const val enteredWords = "enteredWords"
   }
}

class EnteredWord : IEnteredWord, NSManagedObject {
   constructor(context: NSManagedObjectContext, word: String) : super(context) {
      this.value = word
   }
   
   override var value: String
      get() = valueForKey("value") as String
      set(value) {
         setValue(value, forKey = "value")
      }
}
