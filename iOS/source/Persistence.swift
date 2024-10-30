//
//  Persistence.swift
//  Free Bee
//
//  Created by G on 9/10/24.
//

import CoreData

struct PersistenceController {
   static let shared = PersistenceController()
   
   static var preview: PersistenceController = {
      let result = PersistenceController(inMemory: true)
      let viewContext = result.container.viewContext
#if DEBUG
      let game1 = Game(context: viewContext)
      GamePreview.toSep_9_2024(game1)
      game1.progress = GameProgress(context: viewContext)
#endif
      
      do {
         try viewContext.save()
      } catch {
         let nsError = error as NSError
         fatalError("Unresolved error \(nsError), \(nsError.userInfo)")
      }
      return result
   }()
   
   let container: NSPersistentContainer
   
   init(inMemory: Bool = false, fileManager: FileManager = FileManager.default) {
      container = NSPersistentContainer(name: "Free_Bee")
      guard let sharedStoreLocation = fileManager.containerURL(forSecurityApplicationGroupIdentifier:  "group.com.letstwinkle")?.appendingPathComponent("freebee.sqlite"),
            let currentStoreLocation = container.persistentStoreDescriptions.first?.url else {
         fatalError("Expected both locations to exist...")
      }
      if inMemory {
         container.persistentStoreDescriptions.first?.url = URL(fileURLWithPath: "/dev/null")
      } else {
         if fileManager.fileExists(atPath: currentStoreLocation.path) && !fileManager.fileExists(atPath: sharedStoreLocation.path) {
            let coordinator = container.persistentStoreCoordinator
            NSLog("Moving Core Data store from app sandbox to app group container")
            do {
               try coordinator.replacePersistentStore(at: sharedStoreLocation, destinationOptions: nil, withPersistentStoreFrom: currentStoreLocation, sourceOptions: nil, type: .sqlite)
               try? coordinator.destroyPersistentStore(at: currentStoreLocation, type: .sqlite, options: nil)
            } catch {
               print("\(error.localizedDescription)")
            }
         } else {
            let description = NSPersistentStoreDescription(url: sharedStoreLocation)
            container.persistentStoreDescriptions = [description]
         }
      }
      
      container.loadPersistentStores(completionHandler: { (storeDescription, error) in
         if let error = error as NSError? {
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
            fatalError("Unresolved error \(error), \(error.userInfo)")
         }
      })
      container.viewContext.automaticallyMergesChangesFromParent = true
   }
}
