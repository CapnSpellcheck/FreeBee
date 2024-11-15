package com.letstwinkle.freebee.database

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.datetime.Instant
import platform.CoreData.NSFetchRequest
import platform.CoreData.NSFetchedResultsController
import platform.CoreData.NSFetchedResultsControllerDelegateProtocol
import platform.CoreData.NSManagedObject
import platform.CoreData.NSPersistentContainer
import platform.CoreData.NSPersistentStoreDescription
import platform.Foundation.*
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class CoreDataDatabase private constructor(val container: NSPersistentContainer) : FreeBeeRepository {
   
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
         return CoreDataDatabase(container)
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
      val game = Game(container.viewContext)
      game.date = date
      game.allowedWords = allowedWords
      game.centerLetterCode = centerLetterCode
      game.otherLetters = otherLetters
      game.geniusScore = geniusScore
      game.maximumScore = maximumScore
      game.progress = GameProgress(container.viewContext)
      val succeeded = container.viewContext.save(null)
      if (!succeeded) {
         NSLog("[CoreDataDatabase] Couldn't save game")
      }
   }
   
   override fun fetchGamesLive(): Flow<List<IGame>> = callbackFlow {
      
      val fetchRequest = gameFetchRequest
      fetchRequest.sortDescriptors = listOf(NSSortDescriptor(key = "date", ascending = false))
      val fetchResultsController = NSFetchedResultsController(
         fetchRequest = fetchRequest,
         managedObjectContext = container.viewContext,
         sectionNameKeyPath = null,
         cacheName = null
      )
      fun sendResults() {
         val wrappedGames = (fetchResultsController.fetchedObjects ?: emptyList<NSManagedObject>())
            .map { gameManagedObject -> Game(gameManagedObject as NSManagedObject) }
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
   
   override suspend fun getStartedGameCount(): Int {
      val request = gameFetchRequest
      request.predicate = NSPredicate.predicateWithFormat("score > 0")
      return container.viewContext.countForFetchRequest(request, null).convert()
   }
   
   override suspend fun getGeniusGameCount(): Int {
      val request = NSFetchRequest("GameProgress")
      request.predicate = NSPredicate.predicateWithFormat("score >= game.geniusScore")
      return container.viewContext.countForFetchRequest(request, null).convert()
   }
   
   override suspend fun getEnteredWordCount(): Int {
      val request = NSFetchRequest("EnteredWord")
      return container.viewContext.countForFetchRequest(request, null).convert()
      
   }
   
   private val gameFetchRequest: NSFetchRequest
      get() = NSFetchRequest("Game")
   
}
