package com.letstwinkle.freebee.database

import com.letstwinkle.freebee.database.swift.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.CoreData.*
import platform.Foundation.*
import platform.darwin.NSObject
import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalForeignApi::class)
class CoreDataDatabase private constructor(@Suppress("MemberVisibilityCanBePrivate") val container: NSPersistentContainer) : FreeBeeRepository {
   
   companion object {
      val shared = create()
      private const val groupID = "group.com.letstwinkle"
      private const val filename = "freebee.sqlite"
      
      fun create(inMemory: Boolean = false): CoreDataDatabase {
         val container = NSPersistentContainer(name = "Free_Bee")
         if (inMemory)
            (container.persistentStoreDescriptions.firstOrNull() as? NSPersistentStoreDescription)
               ?.URL = NSURL.fileURLWithPath("/dev/null")
         else {
            // use app group storage, to support coexistence with the pure-iOS app I made first
            val sharedStoreURL = NSFileManager.defaultManager
               .containerURLForSecurityApplicationGroupIdentifier(groupID)
               ?.URLByAppendingPathComponent(filename)
            if (sharedStoreURL != null) {
               val sharedStoreDescription = NSPersistentStoreDescription(uRL = sharedStoreURL)
               container.persistentStoreDescriptions = listOf(sharedStoreDescription)
            } else {
               NSLog("CoreDataDatabase.create: could not find container URL for security application group identifier ${groupID}. Using default persistent store description, which may use the wrong URL: ${(container.persistentStoreDescriptions.firstOrNull() as? NSPersistentStoreDescription)?.URL?.description}")
            }
         }
         
         container.loadPersistentStoresWithCompletionHandler { _, nsError ->
            nsError?.let {
               // Replace this implementation with code to handle the error appropriately.
               // fatalError() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
               
               /*
             Typical reasons for an error here include:
             * The parent directory does not exist, cannot be created, or disallows writing.
             * The persistent store is not accessible, due to permissions or data protection when the device is locked.
             * The device is out of space.
             * The store could not be migrated to the current model version.
             Check the error message to determine what the actual problem was.
             */
               throw AssertionError(message = "Unresolved error - Code: ${it.code}, description: ${it.localizedDescription}")
            }
         }
         return CoreDataDatabase(container).debugSeed()
      }
   }
   
   override suspend fun createGame(
      date: Instant,
      allowedWords: Set<String>,
      centerLetterCode: Int,
      otherLetters: String,
      geniusScore: Short,
      maximumScore: Short,
   ) {
      val gameManagedObject = CDGame(container.viewContext)
      gameManagedObject.setDate(date.toNSDate())
      gameManagedObject.setAllowedWords(allowedWords)
      gameManagedObject.setCenterLetterCode(centerLetterCode)
      gameManagedObject.setOtherLetters(otherLetters)
      gameManagedObject.setGeniusScore(geniusScore)
      gameManagedObject.setMaximumScore(maximumScore)
      
      memScoped {
         val error = alloc<ObjCObjectVar<NSError?>>()
         val succeeded = container.viewContext.save(error.ptr)
         if (!succeeded) {
            NSLog("[CoreDataDatabase] Couldn't save game: %@", error.value)
         }
      }
   }
   
   override fun fetchGamesLive(): Flow<List<Game>> = callbackFlow {
      val fetchRequest = CDGame.fetchRequest()
      fetchRequest.sortDescriptors = listOf(NSSortDescriptor(key = "date", ascending = false))
      val fetchResultsController = NSFetchedResultsController(
         fetchRequest = fetchRequest,
         managedObjectContext = container.viewContext,
         sectionNameKeyPath = null,
         cacheName = null
      )
      fun sendResults() {
         val wrappedGames = (fetchResultsController.fetchedObjects ?: emptyList<NSManagedObject>())
            .map { gameManagedObject -> Game(gameManagedObject as CDGame) }
         trySendBlocking(wrappedGames)
      }
      val delegate = object : NSObject(), NSFetchedResultsControllerDelegateProtocol {
         override fun controllerDidChangeContent(controller: NSFetchedResultsController) {
            NSLog("[CoreDataDatabase] NSFetchedResultsControllerDelegate received controllerDidChangeContent")
            sendResults()
         }
      }
      fetchResultsController.delegate = delegate

      val succeeded = fetchResultsController.performFetch(null)
      if (!succeeded) {
         NSLog("[CoreDataDatabase] Couldn't fetch games")
      }
      sendResults()
      
      awaitClose { 
         fetchResultsController.delegate = null
      }
   }.buffer(2)
   
   override suspend fun fetchGameWithWords(gameID: EntityIdentifier): GameWithWords {
      return Game(container.viewContext.objectWithID(gameID) as CDGame).withWords()
   }
   
   override suspend fun getStartedGameCount(): Int {
      val request = CDGameProgress.fetchRequest()
      request.predicate = NSPredicate.predicateWithFormat("score > 0")
      return container.viewContext.countForFetchRequest(request, null).convert()
   }
   
   override suspend fun getGeniusGameCount(): Int {
      val request = CDGameProgress.fetchRequest()
      request.predicate = NSPredicate.predicateWithFormat("score >= game.geniusScore")
      return container.viewContext.countForFetchRequest(request, null).convert()
   }
   
   override suspend fun getEnteredWordCount(): Int {
      val request = CDEnteredWord.fetchRequest()
      return container.viewContext.countForFetchRequest(request, null).convert()
   }
   
   override suspend fun updateGameScore(game: GameWithWords, score: Short) {
      game.game.score = score
   }
   
   override suspend fun addEnteredWord(gameWithWords: GameWithWords, word: String): Boolean {
      val enteredWord = CDEnteredWord(container.viewContext, word)
      enteredWord.setValue(word)
      gameWithWords.game.cdGame.progress().addEnteredWordsObject(enteredWord)
      return container.viewContext.save(null)
   }
   
   override suspend fun
      executeAndSave(transaction: suspend (FreeBeeRepository) -> Unit): Boolean
   {
      val success: Boolean
      withContext(Dispatchers.Main) {
         val viewContext = container.viewContext
         transaction(this@CoreDataDatabase)
         success = viewContext.save(null)
         if (!success)
            viewContext.rollback()
      }
      return success
   }

   @OptIn(ExperimentalNativeApi::class)
   private fun debugSeed(): CoreDataDatabase {
      if (Platform.isDebugBinary) {
         GlobalScope.launch(Dispatchers.Main) {
            val count = 
               container.viewContext.countForFetchRequest(NSFetchRequest("Game"), null)
            if (count.convert<Long>() > 0)
               return@launch
            createGame(
               date = Instant.fromEpochSeconds(1725840000),
               allowedWords = setOf(
                  "accept",
                  "acetate",
                  "affect",
                  "cafe",
                  "cape",
                  "effect",
                  "face",
                  "facet",
                  "fate",
                  "feet",
                  "pace"
               ),
               centerLetterCode = 'e'.code,
               otherLetters = "yatpcf",
               geniusScore = 89,
               maximumScore = 127,
            )
            
            createGame(
               date = Instant.fromEpochSeconds(1540166400),
               allowedWords = setOf(
                  "accrual",
                  "accuracy",
                  "actual",
                  "actually",
                  "actuary",
                  "aura",
                  "aural",
                  "cull",
                  "cult",
                  "cultural",
                  "culturally",
                  "curl",
                  "curly",
                  "curry",
                  "curt",
                  "lull",
                  "rural",
                  "rutty",
                  "tactual",
                  "taut",
                  "truly",
                  "tutu",
                  "yucca"
               ),
               centerLetterCode = 'u'.code,
               otherLetters = "rlcayt",
               geniusScore = 113,
               maximumScore = 161,
            )
            
            createGame(
               date = Instant.fromEpochSeconds(1614902400),
               allowedWords = setOf(
                  "acacia",
                  "arch",
                  "archaic",
                  "arctic",
                  "attach",
                  "attic",
                  "attract",
                  "carat",
                  "cart",
                  "cataract",
                  "catch",
                  "cathartic",
                  "chair",
                  "charm",
                  "chart",
                  "chat",
                  "chia",
                  "chit",
                  "chitchat",
                  "citric",
                  "cram",
                  "critic",
                  "hatch",
                  "itch",
                  "march",
                  "match",
                  "mimic",
                  "rich",
                  "tactic",
                  "tract"
               ),
               centerLetterCode = 'c'.code,
               otherLetters = "mihatr",
               geniusScore = 186,
               maximumScore = 266,
            )
         }
      }
      return this
   }
}

